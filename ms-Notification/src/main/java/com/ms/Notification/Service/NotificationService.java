package com.ms.Notification.Service;

import com.ms.Notification.Model.NotificationCreateDTO;
import com.ms.Notification.Model.NotificationModel;
import com.ms.Notification.Model.NotificationResponseDTO;
import com.ms.Notification.Repository.NotificationRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public NotificationResponseDTO createNotification(NotificationCreateDTO dto, String serviceOrigin) {
        try {
            NotificationModel notification = NotificationModel.builder()
                    .recipientUsername(dto.getRecipientUsername())
                    .senderUsername(dto.getSenderUsername())
                    .type(dto.getType())
                    .serviceOrigin(serviceOrigin)
                    .message(dto.getMessage())
                    .relatedId(dto.getRelatedId())
                    .build();

            NotificationModel saved = notificationRepository.save(notification);

            log.info("Notificaciòn creada exitosamente: Destinatario [{}] - Origen [{}]",
                    saved.getRecipientUsername(), saved.getServiceOrigin());

            return mapToResponseDTO(saved);
        } catch (Exception e) {
            log.error("Error crítico al persistir la notificación: {}", e.getMessage(), e);
            throw new RuntimeException("Fallo al guardar el registro de notificación", e);
        }
    }

    @Transactional(readOnly = true)
    public List<NotificationResponseDTO> getUserNotifications(String username) {
        List<NotificationModel> notifications = notificationRepository.findByRecipientUsernameOrderByCreatedAtDesc(username);

        return notifications.stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Long getUnreadCount(String username) {
        return notificationRepository.countByRecipientUsernameAndIsReadFalse(username);
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        NotificationModel notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("La notificacion con ID " + notificationId + "no existe"));

        notification.setIsRead(true);
        notificationRepository.save(notification);

        log.info("Notificacion [{}] marcada como leida", notificationId);
    }

    private NotificationResponseDTO mapToResponseDTO(NotificationModel model) {
        return NotificationResponseDTO.builder()
                .id(model.getId())
                .senderUsername(model.getSenderUsername())
                .type(model.getType())
                .message(model.getMessage())
                .relatedId(model.getRelatedId())
                .isRead(model.getIsRead())
                .createdAt(model.getCreatedAt())
                .build();
    }

}
