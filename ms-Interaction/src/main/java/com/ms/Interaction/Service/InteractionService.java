package com.ms.Interaction.Service;

import com.ms.Interaction.Client.NotificationClient;
import com.ms.Interaction.Client.PostClient;
import com.ms.Interaction.Client.UserClient;
import com.ms.Interaction.Model.*;
import com.ms.Interaction.Repository.InteractionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InteractionService {

    private final InteractionRepository interactionRepository;
    private final UserClient userClient;
    private final PostClient postClient;
    private final NotificationClient notificationClient;

    @Transactional
    public InteractionResponseDTO toggleLike(InteractionRequestDTO request, Long userId, String token) {

        Optional<InteractionModel> existingLike = interactionRepository
                .findByEntityIdAndUserIdAndEntityType((request.getPostId()), userId, "LIKE");

        if (existingLike.isPresent()) {
            interactionRepository.delete(existingLike.get());
            return new InteractionResponseDTO("LIKE_REMOVED", request.getPostId(), null);
        } else {
            InteractionModel newLike = InteractionModel.builder()
                    .entityId(request.getPostId())
                    .userId(userId)
                    .entityType("LIKE")
                    .build();
            interactionRepository.save(newLike);

            dispararNotificacion(request.getPostId(), userId, token);

            UserDTO userDto = obtenerUsuario(userId, token);
            return new InteractionResponseDTO("LIKE_ADDED", request.getPostId(), userDto);
        }
    }

    @Transactional(readOnly = true)
    public List<InteractionResponseDTO> getLikesForPost(Long postId, String token) {
        List<InteractionModel> likes = interactionRepository.findByEntityIdAndEntityType(postId, "LIKE");

        Map<Long, UserDTO> userCache = new HashMap<>();

        return likes.stream().map(like -> {
            UserDTO userDto = userCache.computeIfAbsent(like.getUserId(),
                    id -> obtenerUsuario(id, token));
            return new InteractionResponseDTO("LIKE_DATA", postId, userDto);
        }).toList();
    }


    private void dispararNotificacion(Long postId, Long userIdLogueado, String token) {
        try {
            Long postAuthorId = postClient.getAuthorIdByPostId(postId, token);

            if (!userIdLogueado.equals(postAuthorId)) {
                NotificationCreateDTO notif = NotificationCreateDTO.builder()
                        .recipientId(postAuthorId)
                        .senderId(userIdLogueado)
                        .type("LIKE")
                        .message("A un usuario le ha gustado tu publicación.")
                        .relatedId(postId)
                        .build();

                notificationClient.enviarNotificacion(notif, "ms-Interaction");
                log.info("Notificación de LIKE enviada al usuario ID [{}]", postAuthorId);
            }
        } catch (Exception e) {
            log.error("Fallo al enviar notificación de LIKE: {}", e.getMessage());
        }
    }

    private UserDTO obtenerUsuario(Long userId, String token) {
        try {
            return userClient.obtenerUsuarioPorId(userId, token);
        } catch (Exception e) {
            log.warn("Error resolviendo UserDTO para ID {}: {}", userId, e.getMessage());
            return new UserDTO(userId, "Usuario Desconocido", "N/A");
        }
    }
}
