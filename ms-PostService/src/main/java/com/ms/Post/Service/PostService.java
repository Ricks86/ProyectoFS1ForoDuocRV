package com.ms.Post.Service;


import com.ms.Post.Model.*;
import com.ms.Post.Repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final WebClient.Builder webClientBuilder;

    @Transactional
    public PostResponseDTO crearPost(PostCreateDTO crear, Long idUsuarioLogueado) {

        Post nuevoPost = Post.builder()
                .titulo(crear.getTitulo())
                .contenido(crear.getContenido())
                .idComunidad(crear.getIdComunidad())
                .idUsuario(idUsuarioLogueado)
                .build();

        Post postGuardado = postRepository.save(nuevoPost);

        UserDTO autorDto;
        //esperar a que el victor tenga el servicio
        try {
            autorDto = webClientBuilder.build()
                    .get()
                    .uri("http://localhost:8082/users/{id}", idUsuarioLogueado)
                    .retrieve()
                    .bodyToMono(UserDTO.class)
                    .block();
        } catch (Exception e) {
            autorDto = new UserDTO(idUsuarioLogueado, "Usuario Temporal", "Alias No Disponible");
        }

        return PostResponseDTO.builder()
                .id(postGuardado.getId())
                .titulo(postGuardado.getTitulo())
                .contenido(postGuardado.getContenido())
                .fechaCreacion(postGuardado.getFechaCreacion())
                .idComunidad(postGuardado.getIdComunidad())
                .idUsuario(idUsuarioLogueado)
                .autor(autorDto)
                .build();
    }

    @Transactional(readOnly = true) //
    public PostResponseDTO obtenerPostPorId(Long id) {

        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("El post con ID " + id + " no existe."));

        UserDTO autorDto;
        try {
            autorDto = webClientBuilder.build()
                    .get()
                    .uri("http://localhost:8082/users/{id}", post.getIdUsuario())
                    .retrieve()
                    .bodyToMono(UserDTO.class)
                    .block();
        } catch (Exception e) {
            autorDto = new UserDTO(post.getIdUsuario(), "Usuario Temporal", "Alias No Disponible");
        }

        return PostResponseDTO.builder()
                .id(post.getId())
                .titulo(post.getTitulo())
                .contenido(post.getContenido())
                .fechaCreacion(post.getFechaCreacion())
                .idComunidad(post.getIdComunidad())
                .autor(autorDto)
                .build();
    }

    @Transactional(readOnly = true)
    public List<PostResponseDTO> obtenerPostsCreadosAntesDe(LocalDateTime fecha) {
        List<Post> posts = postRepository.findByFechaCreacionBefore(fecha);

        return posts.stream()
                .map(post -> {
                    UserDTO autorDto;
                    try {
                        autorDto = webClientBuilder.build()
                                .get()
                                .uri("http://localhost:8082/users/{id}", post.getIdUsuario())
                                .retrieve()
                                .bodyToMono(UserDTO.class)
                                .block();
                    } catch (Exception e) {
                        autorDto = new UserDTO(post.getIdUsuario(), "Usuario Temporal", "Alias No Disponible");
                    }

                    return PostResponseDTO.builder()
                            .id(post.getId())
                            .titulo(post.getTitulo())
                            .contenido(post.getContenido())
                            .fechaCreacion(post.getFechaCreacion())
                            .idComunidad(post.getIdComunidad())
                            .autor(autorDto)
                            .build();
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PostResponseDTO> obtenerPostsPorUsername(String username) {

        UserDTO autorDto;
        //pedirle al victor que haga ese endpoint
        try {
            autorDto = webClientBuilder.build()
                    .get()
                    .uri("http://localhost:8082/users/username/{username}", username)
                    .retrieve()
                    .bodyToMono(UserDTO.class)
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("El usuario '@" + username + "' no existe en el DuckyProtocol.");
        }

        List<Post> posts = postRepository.findByIdUsuario(autorDto.getId());

        return posts.stream()
                .map(post -> PostResponseDTO.builder()
                        .id(post.getId())
                        .titulo(post.getTitulo())
                        .contenido(post.getContenido())
                        .fechaCreacion(post.getFechaCreacion())
                        .idComunidad(post.getIdComunidad())
                        .autor(autorDto) // Inyectamos el mismo autor a todos sus posts
                        .build())
                .toList();
    }

    public Page<PostFeedDTO> getFeedPaginado(Pageable pageable) {
        Page<Post> postsPage = postRepository.findAllByOrderByFechaCreacionAsc(pageable);

        return postsPage.map(post -> {
            UserDTO autorDto = null;
            try {

                autorDto = webClientBuilder.build()
                        .get()
                        .uri("http://localhost:8082/users/{id}", post.getIdUsuario())
                        .retrieve()
                        .bodyToMono(UserDTO.class)
                        .block();
            } catch (Exception e) {
                autorDto = new UserDTO(post.getIdUsuario(), "Usuario Desconocido", "N/A");
            }

            return new PostFeedDTO(
                    post.getId(),
                    post.getTitulo(),
                    post.getContenido(),
                    post.getFechaCreacion(),
                    autorDto
            );
        });
    }

}