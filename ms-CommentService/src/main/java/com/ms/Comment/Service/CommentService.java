package com.ms.Comment.Service;

import com.ms.Comment.Model.Comment;
import com.ms.Comment.Model.CommentRequestDTO;
import com.ms.Comment.Model.CommentResponseDTO;
import com.ms.Comment.Repository.CommentRepository;
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
public class CommentService {

    private final CommentRepository commentRepository;
    private final WebClient.Builder webClientBuilder;

    @Transactional
    public CommentResponseDTO crearComentario(CommentRequestDTO dto) {
        log.info("Intentando crear comentario para Post Id: {} por usuario ID: {}", dto.getPostId(), dto.getUserId());

        //Cambiar por usuario en futuras actualizaciones
        Boolean userExists = checkExists("http://localhost:8081/api/auth/check/" + dto.getUserId());

        Boolean postExists = checkExists("http://localhost:8083/api/posts/check/" + dto.getPostId());

        if (!userExists || !postExists) {
            log.info("Usuario o post no existe");
            throw new RuntimeException("Usuario o post no existe");
        }

        Comment comment = Comment.builder()
                .content(dto.getContent())
                .userId(dto.getUserId())
                .postId(dto.getPostId())
                .build();

        Comment savedComment = commentRepository.save(comment);
        log.info("Comentario creado con ID: {}", savedComment.getId());
        return mapToDTO(savedComment);
    }

    @Transactional(readOnly = true)
    public List<CommentResponseDTO> getComentarioByPostId(Long postId) {
        log.info("Intentando listar comentarios del post {}", postId);

        return commentRepository.findByPostId(postId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private Boolean checkExists(String url) {
        return webClientBuilder.build().get().uri(url).retrieve()
                .bodyToMono(Boolean.class).block();
    }
    private CommentResponseDTO mapToDTO(Comment comment) {
        return CommentResponseDTO.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .userId(comment.getUserId())
                .postId(comment.getPostId())
                .fechaCreacion(comment.getCreatedAt())
                .build();
    }

}
