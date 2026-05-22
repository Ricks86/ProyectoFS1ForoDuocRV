package com.ms.Notification.Controller;

import com.ms.Notification.Model.NotificationCreateDTO;
import com.ms.Notification.Model.NotificationResponseDTO;
import com.ms.Notification.Service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<NotificationResponseDTO> createNotification(
            @Valid @RequestBody NotificationCreateDTO request,
            @RequestHeader(value = "X-Service-Origin", defaultValue = "SYSTEM") String serviceOrigin) {

        NotificationResponseDTO nuevaNotificacion = notificationService.createNotification(request, serviceOrigin);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaNotificacion);
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<List<NotificationResponseDTO>> getUserNotifications(@PathVariable String username) {
        return ResponseEntity.ok(notificationService.getUserNotifications(username));
    }

    @GetMapping("/user/{username}/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@PathVariable String username) {
        Long count = notificationService.getUnreadCount(username);
        return ResponseEntity.ok(Map.of("UnreadCount", count));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Map<String, String>> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);

        Map<String, String> respuesta = Map.of(
                "mensaje", "Notificación marcada como leída correctamente",
                "estado", "EXITOSO"
        );

        return ResponseEntity.ok(respuesta);
    }

}
