package com.ms.Interaction.Service;

import com.ms.Interaction.Client.CommentClient;
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
    private final CommentClient commentClient;

    @Transactional
    public InteractionResponseDTO toggleVote(InteractionRequestDTO request, Long userId, String token) {

        String eType = request.getEntityType().toUpperCase();
        String vType = request.getVoteType().toUpperCase();
        Long eId = request.getEntityId();

        Optional<InteractionModel> existingVote = interactionRepository
                .findByEntityIdAndUserIdAndEntityType(eId, userId, eType);

        if (existingVote.isPresent()) {
            InteractionModel vote = existingVote.get();

            if (vote.getVoteType().equals(vType)) {
                interactionRepository.delete(vote);
                return InteractionResponseDTO.builder()
                        .status("VOTE_REMOVED").entityType(eType).entityId(eId).build();
            } else {
                vote.setVoteType(vType);
                interactionRepository.save(vote);

                UserDTO userDto = obtenerUsuario(userId, token);
                return InteractionResponseDTO.builder()
                        .status("VOTE_UPDATED").entityType(eType).entityId(eId).voteType(vType).user(userDto).build();
            }
        } else {
            InteractionModel newVote = InteractionModel.builder()
                    .entityId(eId)
                    .userId(userId)
                    .entityType(eType)
                    .voteType(vType)
                    .build();

            interactionRepository.save(newVote);

            if (vType.equals("UPVOTE") || vType.equals("LIKE")) {
                dispararNotificacion(eType, eId, userId, token);
            }

            UserDTO userDto = obtenerUsuario(userId, token);
            return InteractionResponseDTO.builder()
                    .status("VOTE_ADDED").entityType(eType).entityId(eId).voteType(vType).user(userDto).build();
        }
    }

    @Transactional(readOnly = true)
    public List<InteractionResponseDTO> getVotesForEntity(String entityType, Long entityId, String token) {

        List<InteractionModel> votes = interactionRepository.findByEntityIdAndEntityType(entityId, entityType.toUpperCase());
        Map<Long, UserDTO> userCache = new HashMap<>();

        return votes.stream().map(vote -> {
            UserDTO userDto = userCache.computeIfAbsent(vote.getUserId(),
                    id -> obtenerUsuario(id, token));

            return InteractionResponseDTO.builder()
                    .status("VOTE_DATA")
                    .entityType(entityType.toUpperCase())
                    .entityId(entityId)
                    .voteType(vote.getVoteType())
                    .user(userDto)
                    .build();
        }).toList();
    }


    private void dispararNotificacion(String entityType, Long entityId, Long userIdLogueado, String token) {
        try {
            Long authorId = null;
            String mensajeNotificacion = "";

            if (entityType.equals("POST")) {
                authorId = postClient.getAuthorIdByPostId(entityId, token);
                mensajeNotificacion = "A un usuario le ha gustado tu publicación.";

            } else if (entityType.equals("COMMENT")) {
                authorId = commentClient.getAuthorIdByCommentId(entityId, token);
                mensajeNotificacion = "A un usuario le ha gustado tu comentario.";
            } else {
                log.warn("Tipo de entidad no soportado para notificaciones: {}", entityType);
                return;
            }

            if (authorId != null && !userIdLogueado.equals(authorId)) {
                NotificationCreateDTO notif = NotificationCreateDTO.builder()
                        .recipientId(authorId)
                        .senderId(userIdLogueado)
                        .type("LIKE")
                        .message(mensajeNotificacion)
                        .relatedId(entityId)
                        .build();

                notificationClient.enviarNotificacion(notif, "ms-Interaction");
                log.info("Notificación de interacción enviada al usuario ID [{}]", authorId);
            }
        } catch (Exception e) {
            log.error("Fallo al enviar notificación de interacción para {} ID {}: {}", entityType, entityId, e.getMessage());
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
