package com.ms.Comment.Service;

import com.ms.Comment.Client.UserClient;
import com.ms.Comment.Model.Comment;
import com.ms.Comment.Model.CommentCreateDTO;
import com.ms.Comment.Model.CommentResponseDTO;
import com.ms.Comment.Model.UserDTO;
import com.ms.Comment.Repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserClient userClient;

    @Transactional
    public CommentResponseDTO crearComentario(CommentCreateDTO request, Long userIdLogueado, String token) {

        Comment nuevoComentario = Comment.builder()
                .content(request.getContent())
                .postId(request.getPostId())
                .userId(userIdLogueado)
                .build();

        Comment comentarioGuardado = commentRepository.save(nuevoComentario);

        UserDTO autorDto = obtenerAutorSeguro(userIdLogueado, token);

        return construirResponseDTO(comentarioGuardado, autorDto);
    }

    @Transactional(readOnly = true)
    public List<CommentResponseDTO> obtenerComentariosPorPostId(Long postId, String token) {

        List<Comment> comentarios = commentRepository.findByPostIdOrderByCreatedAtAsc(postId);

        Map<Long, UserDTO> userCache = new HashMap<>();

        return comentarios.stream()
                .map(comentario -> {
                    UserDTO autorDto = userCache.computeIfAbsent(comentario.getUserId(),
                            id -> obtenerAutorSeguro(id, token));
                    return construirResponseDTO(comentario, autorDto);
                })
                .toList();
    }

    private UserDTO obtenerAutorSeguro(Long userId, String token) {
        try {
            return userClient.obtenerUsuarioPorId(userId, token);
        } catch (Exception e) {
            log.warn("Fallo al contactar ms-User para ID {}: {}", userId, e.getMessage());
            return new UserDTO(userId, "Usuario Desconocido", "Alias No Disponible");
        }
    }

    private CommentResponseDTO construirResponseDTO(Comment comentario, UserDTO autor) {
        return CommentResponseDTO.builder()
                .id(comentario.getId())
                .content(comentario.getContent())
                .postId(comentario.getPostId())
                .userId(comentario.getUserId())
                .createdAt(comentario.getCreatedAt())
                .autor(autor)
                .build();
    }
}