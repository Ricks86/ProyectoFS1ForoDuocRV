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


/**
 * Servicio central para la gestión de notificaciones.
 * <p>
 * Se encarga de la persistencia de las alertas, el cálculo de métricas (no leídas)
 * y la integración síncrona con el microservicio ms-User para componer
 * la información del remitente en tiempo real mediante el patrón API Composition.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserClient userClient;

    /**
     * Persiste una nueva alerta en el sistema con el origen del servicio generador.
     *
     * @param dto Objeto con la data esencial de la notificación (mensaje, destinatario, remitente).
     * @param serviceOrigin Nombre del microservicio que disparó la alerta.
     * @return NotificationResponseDTO confirmando la creación.
     */
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

    /**
     * Consulta las notificaciones de un usuario y compone al emisor.
     *
     * @param recipientId Identificador del usuario que consulta su bandeja.
     * @param token Token JWT para su propagación.
     * @return Lista de NotificationResponseDTO mapeada.
     */
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

    /**
     * Realiza un conteo rápido de las alertas pendientes.
     *
     * @param recipientId Identificador del usuario.
     * @return Cantidad entera de notificaciones no leídas.
     */
    @Transactional(readOnly = true)
    public Long getUnreadCount(Long recipientId) {
        return notificationRepository.countByRecipientIdAndIsReadFalse(recipientId);
    }

    /**
     * Modifica el indicador de lectura de una notificación validando autorización.
     *
     * @param notificationId Identificador de alerta a modificar.
     * @param idUsuarioLogueado Identificador del usuario dueño validado en el controller.
     */
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

    /**
     * Consulta los datos del usuario mediante el Feign Client.
     * Implementa un fallback para retornar un usuario genérico del sistema en caso de error.
     *
     * @param userId Identificador del usuario.
     * @param token Token JWT de autorización.
     * @return UserDTO con la información del usuario o valores por defecto.
     */
    private UserDTO obtenerUsuario(Long userId, String token) {
        try {
            return userClient.obtenerUsuarioPorId(userId, token);
        } catch (Exception e) {
            log.warn("Fallo al contactar ms-User. ID {}: {}", userId, e.getMessage());
            return new UserDTO(userId, "Sistema", "Foro");
        }
    }

    /**
     * Convierte la entidad de base de datos a un objeto de transferencia (DTO)
     *
     * @param model Entidad NotificationModel obtenida de la base de datos.
     * @param sender DTO del usuario remitente, si existe.
     * @return NotificationResponseDTO listo para ser enviado al cliente.
     */
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
