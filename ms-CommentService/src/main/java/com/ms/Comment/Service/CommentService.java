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

/**
 * Servicio central para el MS Comment.
 * <p>
 * Administra la creación y recuperación de respuestas a los posts.
 * Se comunica asíncronamente con ms-Notification
 * para alertar a los autores, y síncronamente con ms-User para poblar
 * los perfiles públicos de quienes comentan.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserClient userClient;
    private final NotificationClient notificationClient;

    /**
     * Crea y persiste un nuevo comentario asociado a un post.
     * <p>
     * Tras guardar en la base de datos, dispara un evento de notificación
     * y consulta la información del autor para retornar el DTO completamente armado.
     *
     * @param request          Objeto DTO que contiene el texto del comentario y el ID del Post destino.
     * @param userIdLogueado   Identificador interno del usuario emisor, extraído desde el token JWT.
     * @param token            Token de autorización para su propagación hacia ms-User.
     * @return                 CommentResponseDTO con la información persistida y los datos del autor incrustados.
     */
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

    /**
     * Recupera todos los comentarios asociados a un post específico.
     *
     * @param postId Identificador único del Post cuyas respuestas se desean recuperar.
     * @param token  Token JWT para validar la conexión interservicios.
     * @return       Lista cronológica de comentarios (CommentResponseDTO) con sus autores resueltos.
     */
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

    /**
     * dispara la emisión de una alerta hacia el dueño de la publicación comentada.
     * <p>
     * El envío se aísla en un bloque try-catch. Si ms-Notification
     * está caído, el error se registra en logs pero no interrumpe el flujo principal,
     * garantizando que el comentario se publique con éxito.
     *
     * @param request          El DTO con la información de la publicación.
     * @param userIdLogueado   El ID de quien comenta (evita autono-tificaciones si el creador comenta su propio post).
     */
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

    /**
     * Método auxiliar de contingencia para resolver la identidad pública de un usuario.
     *
     * @param userId Identificador del dueño del comentario.
     * @param token  Token JWT en tránsito.
     * @return       Objeto UserDTO real o una versión por defecto si hay latencia/falla.
     */
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
    /**
     * Consulta interna utilizada por otros microservicios de la red.
     * <p>
     * Devuelve rápidamente el identificador del dueño de un comentario específico.
     *
     * @param commentId ID del comentario a consultar.
     * @return          Long que representa el ID interno del autor.
     * @throws RuntimeException Si el ID del comentario no existe en los registros.
     */
    @Transactional(readOnly = true)
    public Long obtenerAutorPorId(Long commentId) {
        Comment comentario = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comentario no encontrado con ID: " + commentId));

        return comentario.getUserId();
    }
}