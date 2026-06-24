package com.ms.Comment.Service;

import com.ms.Comment.Client.NotificationClient;
import com.ms.Comment.Client.UserClient;
import com.ms.Comment.DTOs.CommentCreateDTO;
import com.ms.Comment.DTOs.CommentResponseDTO;
import com.ms.Comment.DTOs.NotificationCreateDTO;
import com.ms.Comment.DTOs.UserDTO;
import com.ms.Comment.Model.*;
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
    private final NotificationClient notificationClient;

    @Transactional
    public CommentResponseDTO crearComentario(CommentCreateDTO request, Long userIdLogueado, String token) {

        Comment nuevoComentario = Comment.builder()
                .content(request.getContent())
                .postId(request.getPostId())
                .userId(userIdLogueado)
                .build();

        Comment comentarioGuardado = commentRepository.save(nuevoComentario);

        dispararNotificacion(request, userIdLogueado);

        UserDTO autorDto = obtenerAutor(userIdLogueado, token);

        return construirResponseDTO(comentarioGuardado, autorDto);
    }

    @Transactional(readOnly = true)
    public List<CommentResponseDTO> obtenerComentariosPorPostId(Long postId, String token) {

        List<Comment> comentarios = commentRepository.findByPostIdOrderByCreatedAtAsc(postId);

        Map<Long, UserDTO> userCache = new HashMap<>();

        return comentarios.stream()
                .map(comentario -> {
                    UserDTO autorDto = userCache.computeIfAbsent(comentario.getUserId(),
                            id -> obtenerAutor(id, token));
                    return construirResponseDTO(comentario, autorDto);
                })
                .toList();
    }

    private void dispararNotificacion(CommentCreateDTO request, Long userIdLogueado) {
        try {
            Long postAuthorId = request.getPostId() ;

            if (!userIdLogueado.equals(postAuthorId)) {
                NotificationCreateDTO notif = NotificationCreateDTO.builder()
                        .recipientId(postAuthorId)
                        .senderId(userIdLogueado)
                        .type("COMMENT")
                        .message("Nuevo comentario en tu publicación: " + request.getContent())
                        .relatedId(request.getPostId())
                        .build();

                notificationClient.enviarNotificacion(notif, "ms-CommentService");
                log.info("Notificación enviada al autor del post: {}", postAuthorId);
            }
        } catch (Exception e) {
            log.error("Fallo al enviar notificación de comentario: {}", e.getMessage());
        }
    }

    private UserDTO obtenerAutor(Long userId, String token) {
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

    @Transactional(readOnly = true)
    public Long obtenerAutorPorId(Long commentId) {
        Comment comentario = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comentario no encontrado con ID: " + commentId));

        return comentario.getUserId();
    }
}