package com.ms.Notification.Service;

import com.ms.Notification.Client.UserClient;
import com.ms.Notification.DTOs.NotificationCreateDTO;
import com.ms.Notification.Model.NotificationModel;
import com.ms.Notification.DTOs.NotificationResponseDTO;
import com.ms.Notification.DTOs.UserDTO;
import com.ms.Notification.Repository.NotificationRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserClient userClient;

    @Transactional
    public NotificationResponseDTO createNotification(NotificationCreateDTO dto, String serviceOrigin) {

        NotificationModel notification = NotificationModel.builder()
                .recipientId(dto.getRecipientId())
                .senderId(dto.getSenderId())
                .type(dto.getType())
                .serviceOrigin(serviceOrigin)
                .message(dto.getMessage())
                .relatedId(dto.getRelatedId())
                .build();

        NotificationModel saved = notificationRepository.save(notification);
        log.info("Notificación creada: Destinatario ID [{}] - Origen [{}]", saved.getRecipientId(), saved.getServiceOrigin());

        return mapToResponseDTO(saved, null);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponseDTO> getUserNotifications(Long recipientId, String token) {
        List<NotificationModel> notifications = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId);

        Map<Long, UserDTO> userCache = new HashMap<>();

        return notifications.stream()
                .map(notif -> {
                    UserDTO sender = null;
                    if (notif.getSenderId() != null) {
                        sender = userCache.computeIfAbsent(notif.getSenderId(), id -> obtenerUsuario(id, token));
                    }
                    return mapToResponseDTO(notif, sender);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public Long getUnreadCount(Long recipientId) {
        return notificationRepository.countByRecipientIdAndIsReadFalse(recipientId);
    }

    @Transactional
    public void markAsRead(Long notificationId, Long idUsuarioLogueado) {
        NotificationModel notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("La notificación con ID " + notificationId + " no existe"));

        if (!notification.getRecipientId().equals(idUsuarioLogueado)) {
            throw new RuntimeException("Acceso denegado: Esta notificación no te pertenece.");
        }

        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    private UserDTO obtenerUsuario(Long userId, String token) {
        try {
            return userClient.obtenerUsuarioPorId(userId, token);
        } catch (Exception e) {
            log.warn("Fallo al contactar ms-User. ID {}: {}", userId, e.getMessage());
            return new UserDTO(userId, "Sistema", "Foro");
        }
    }

    private NotificationResponseDTO mapToResponseDTO(NotificationModel model, UserDTO sender) {
        return NotificationResponseDTO.builder()
                .id(model.getId())
                .recipientId(model.getRecipientId())
                .sender(sender)
                .type(model.getType())
                .message(model.getMessage())
                .relatedId(model.getRelatedId())
                .isRead(model.getIsRead())
                .createdAt(model.getCreatedAt())
                .build();
    }

}
