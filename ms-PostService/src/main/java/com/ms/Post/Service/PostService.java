package com.ms.Post.Service;


import com.ms.Post.Client.UserClient;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final PostRepository postRepository;
    private final UserClient userClient;

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

    @Transactional(readOnly = true)
    public PostResponseDTO obtenerPostPorId(Long id, String token) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("El post con ID " + id + " no existe."));

        UserDTO autorDto = obtenerAutor(post.getIdUsuario(), token);
        return construirPostResponse(post, autorDto);
    }

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

    @Transactional(readOnly = true)
    public Long getAuthorIdByPostId(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post no encontrado con ID: " + postId));

        return post.getIdUsuario();
    }

}