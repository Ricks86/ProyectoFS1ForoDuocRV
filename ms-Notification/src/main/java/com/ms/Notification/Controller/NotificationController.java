package com.ms.Notification.Controller;

import com.ms.Notification.Model.NotificationCreateDTO;
import com.ms.Notification.Model.NotificationResponseDTO;
import com.ms.Notification.Security.JwtUtil;
import com.ms.Notification.Service.AuditService;
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
    private final JwtUtil jwtUtil;
    private final AuditService auditoriaService;

    @PostMapping
    public ResponseEntity<NotificationResponseDTO> createNotification(
            @Valid @RequestBody NotificationCreateDTO request,
            @RequestHeader(value = "X-Service-Origin", defaultValue = "SYSTEM") String serviceOrigin) {

        NotificationResponseDTO nuevaNotificacion = notificationService.createNotification(request, serviceOrigin);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaNotificacion);
    }

    @GetMapping("/mis-notificaciones")
    public ResponseEntity<List<NotificationResponseDTO>> getMyNotifications(@RequestHeader("Authorization") String token) {
        Long myId = jwtUtil.extractUserId(token);
        return ResponseEntity.ok(notificationService.getUserNotifications(myId, token));
    }

    @GetMapping("/no-leidas")
    public ResponseEntity<Map<String, Long>> getMyUnreadCount(@RequestHeader("Authorization") String token) {
        Long myId = jwtUtil.extractUserId(token);
        Long count = notificationService.getUnreadCount(myId);
        return ResponseEntity.ok(Map.of("UnreadCount", count));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Map<String, String>> markAsRead(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {

        Long myId = jwtUtil.extractUserId(token);

        notificationService.markAsRead(id, myId);

        auditoriaService.registrarLog(
                myId,
                "READ_NOTIFICATION",
                "El usuario marcó la alerta ID [" + id + "] como leída."
        );

        return ResponseEntity.ok(Map.of(
                "mensaje", "Notificación marcada como leída",
                "estado", "EXITOSO"
        ));
    }

}
