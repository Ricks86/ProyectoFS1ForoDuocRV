package com.ms.Post.Service;


import com.ms.Post.Client.UserClient;
import com.ms.Post.DTOs.PostCreateDTO;
import com.ms.Post.DTOs.PostFeedDTO;
import com.ms.Post.DTOs.PostResponseDTO;
import com.ms.Post.DTOs.UserDTO;
import com.ms.Post.Model.*;
import com.ms.Post.Repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio central para la gestión de los Posts.
 * <p>
 * Se encarga de la persistencia del contenido, la paginación del feed principal
 * y la integración síncrona con el microservicio ms-User para componer
 * la información del autor en tiempo real mediante el patrón API Composition.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final PostRepository postRepository;
    private final UserClient userClient;

    /**
     * Crea una nueva publicación y la vincula al usuario autenticado.
     *
     * @param crear             Objeto DTO con el título, contenido e ID de la comunidad.
     * @param idUsuarioLogueado Identificador del autor extraído de forma segura desde el token JWT.
     * @param token             Token de autorización para propagarlo hacia ms-User si es necesario.
     * @return                  PostResponseDTO con los datos guardados y la información pública del autor.
     */
    @Transactional
    public PostResponseDTO crearPost(PostCreateDTO crear, Long idUsuarioLogueado, String token) {

        Post nuevoPost = Post.builder()
                .titulo(crear.getTitulo())
                .contenido(crear.getContenido())
                .idComunidad(crear.getIdComunidad())
                .idUsuario(idUsuarioLogueado)
                .build();

        Post postGuardado = postRepository.save(nuevoPost);
        UserDTO autorDto = obtenerAutor(idUsuarioLogueado, token);

        return construirPostResponse(postGuardado, autorDto);
    }

    /**
     * Recupera una publicación específica por su ID.
     *
     * @param id    Identificador único de la publicación.
     * @param token Token JWT del solicitante.
     * @return      PostResponseDTO con la información consolidada.
     * @throws RuntimeException Si el post solicitado no existe en la base de datos.
     */
    @Transactional(readOnly = true)
    public PostResponseDTO obtenerPostPorId(Long id, String token) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("El post con ID " + id + " no existe."));

        UserDTO autorDto = obtenerAutor(post.getIdUsuario(), token);
        return construirPostResponse(post, autorDto);
    }

    /**
     * Recupera una lista histórica de publicaciones creadas antes de una fecha específica.
     * @param fecha Límite cronológico superior para la consulta.
     * @param token Token JWT para validación interservicios.
     * @return      Lista de PostResponseDTO mapeada con sus respectivos autores.
     */
    @Transactional(readOnly = true)
    public List<PostResponseDTO> obtenerPostsCreadosAntesDe(LocalDateTime fecha, String token) {
        List<Post> posts = postRepository.findByFechaCreacionBefore(fecha);

        Map<Long, UserDTO> userCache = new HashMap<>();

        return posts.stream()
                .map(post -> {
                    UserDTO autorDto = userCache.computeIfAbsent(post.getIdUsuario(),
                            id -> obtenerAutor(id, token));
                    return construirPostResponse(post, autorDto);
                })
                .toList();
    }

    /**
     * Recupera todas las publicaciones escritas por un nombre de usuario en particular.
     * <p>
     * Invoca de forma síncrona a ms-User para resolver el username y obtener su ID interno.
     * Si el usuario no existe en la arquitectura, aborta la operación antes de consultar la base de datos.
     *
     * @param username Nombre de usuario a consultar.
     * @param token    Token JWT del solicitante.
     * @return         Lista de publicaciones pertenecientes al usuario.
     * @throws RuntimeException Si ms-User responde que el usuario no existe.
     */
    @Transactional(readOnly = true)
    public List<PostResponseDTO> obtenerPostsPorUsername(String username, String token) {
        UserDTO autorDto;
        try {
            autorDto = userClient.obtenerUsuarioPorUsername(username, token);
        } catch (Exception e) {
            log.error("Error al buscar usuario [{}]: {}", username, e.getMessage());
            throw new RuntimeException("El usuario '@" + username + "' no existe en el sistema.");
        }

        List<Post> posts = postRepository.findByIdUsuario(autorDto.getId());

        return posts.stream()
                .map(post -> construirPostResponse(post, autorDto))
                .toList();
    }

    /**
     * Genera un feed paginado de publicaciones ordenado cronológicamente.
     *
     * @param pageable Objeto que contiene la configuración de la página (tamaño, número de página).
     * @param token    Token JWT para autorizar las llamadas subyacentes.
     * @return         Page con elementos PostFeedDTO listos para ser renderizados por el cliente.
     */
    @Transactional(readOnly = true)
    public Page<PostFeedDTO> obtenerFeedPaginado(Pageable pageable, String token) {
        Page<Post> postsPage = postRepository.findAllByOrderByFechaCreacionAsc(pageable);

        Map<Long, UserDTO> userCache = new HashMap<>();

        return postsPage.map(post -> {
            UserDTO autorDto = userCache.computeIfAbsent(post.getIdUsuario(),
                    id -> obtenerAutor(id, token));

            return new PostFeedDTO(
                    post.getId(),
                    post.getTitulo(),
                    post.getContenido(),
                    post.getFechaCreacion(),
                    autorDto
            );
        });
    }

    /**
     * Método auxiliar para recuperar la información pública de un usuario mediante Feign Client.
     * <p>
     * Posee un bloque try-catch que actúa como Fallback.
     * Si el microservicio ms-User colapsa o tarda demasiado, este método captura la excepción
     * y retorna un "Usuario Temporal", para evitar errores 500.
     *
     * @param userId Identificador interno del usuario.
     * @param token  Token JWT para la autorización.
     * @return       UserDTO con los datos del usuario real o un usuario por defecto en caso de caída.
     */
    private UserDTO obtenerAutor(Long userId, String token) {
        try {
            return userClient.obtenerUsuarioPorId(userId, token);
        } catch (Exception e) {
            log.warn("Fallo al obtener ms-User para ID {}: {}", userId, e.getMessage());
            return new UserDTO(userId, "Usuario Temporal", "Alias No Disponible");
        }
    }

    private PostResponseDTO construirPostResponse(Post post, UserDTO autor) {
        return PostResponseDTO.builder()
                .id(post.getId())
                .titulo(post.getTitulo())
                .contenido(post.getContenido())
                .fechaCreacion(post.getFechaCreacion())
                .idComunidad(post.getIdComunidad())
                .autor(autor)
                .build();
    }

    /**
     * Extrae el identificador del autor de una publicación.
     *
     * @param postId Identificador de la publicación.
     * @return       ID del usuario que creó el post.
     * @throws RuntimeException Si la publicación consultada no existe.
     */
    @Transactional(readOnly = true)
    public Long getAuthorIdByPostId(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post no encontrado con ID: " + postId));

        return post.getIdUsuario();
    }
}