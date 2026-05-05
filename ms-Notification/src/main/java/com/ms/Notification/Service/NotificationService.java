package com.ms.Notification.Service;

import com.ms.Notification.Model.NotificationCreateDTO;
import com.ms.Notification.Model.NotificationModel;
import com.ms.Notification.Model.NotificationResponseDTO;
import com.ms.Notification.Repository.NotificationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public NotificationResponseDTO createNotification(NotificationCreateDTO createDTO) {
        log.info("Creando notificacion de tipo {} para el usuario: {}", createDTO.getType());

        NotificationModel notification = NotificationModel.builder()
                .recipientUsername(createDTO.getRecipientUsername())
                .senderUsername(createDTO.getSenderUsername())
                .type(createDTO.getType())
                .message(createDTO.getMessage())
                .relatedId(createDTO.getRelatedId())
                .build();

        NotificationModel saved = notificationRepository.save(notification);
        return mapToResponseDTO(saved);
    }

    public List<NotificationResponseDTO> getUserNotifications(String username) {
        log.info("Obteniendo notificaciones para: {}", username);
        return notificationRepository.findByRecipientUsernameOrderByCreatedAtDesc(username)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public Long getUnreadCount(String username) {
        return notificationRepository.countByRecipientUsernameAndIsReadFalse(username);
    }

    @Transactional
    public void markAsRead(Long id) {
        log.info("Marcando notificación {} como leída", id);
        NotificationModel notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notificación no encontrada"));

        notification.setIsRead(true);
        notificationRepository.save(notification);
    }


}
