package com.ms.Post.Service;


import com.ms.Post.Model.Post;
import com.ms.Post.Model.PostRequestDTO;
import com.ms.Post.Model.PostResponseDTO;
import com.ms.Post.Repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final WebClient.Builder webClientBuilder;

    @Transactional
    public PostResponseDTO crearPost(PostRequestDTO dto) {
        log.info("Creando post para el user: {}", dto.getIdUsuario());

        Boolean userExiste = webClientBuilder.build()
                .get()
                .uri("http://localhost:8081/api/usuarios/{id}", dto.getIdUsuario())
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();

        if (userExiste == null || !userExiste) {
            log.error("Usuario con ID {} no existe", dto.getIdUsuario());
            throw new RuntimeException("Usuario no existe");
        }

        Post post = Post.builder()
                .titulo(dto.getTitulo())
                .contenido(dto.getContenido())
                .idUsuario(dto.getIdUsuario())
                .idComunidad(dto.getIdComunidad())
                .build();

        Post savedPost = postRepository.save(post);
        log.info("Post saved: {}", savedPost.getId(), savedPost.getIdComunidad());

        return mapToResponseDTO(savedPost);
    }

    @Transactional(readOnly = true)
    public List<PostResponseDTO> listarPorComunidadId(Long idComunidad) {
        log.info("Listando posts para el comunidad: {}", idComunidad);
        return postRepository.findByIdComunidadOrderByFechaCreacionDesc(idComunidad)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PostResponseDTO> listarPost() {
        log.info("Listando todos los posts");
        return postRepository.findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    private PostResponseDTO mapToResponseDTO(Post post){
        return PostResponseDTO.builder()
                .id(post.getId())
                .titulo(post.getTitulo())
                .contenido(post.getContenido())
                .idUsuario(post.getIdUsuario())
                .idComunidad(post.getIdComunidad())
                .fechaCreacion(post.getFechaCreacion())
                .build();
    }
}
