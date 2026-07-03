package com.ms.Notification.Controller;

import com.ms.Notification.DTOs.NotificationCreateDTO;
import com.ms.Notification.DTOs.NotificationResponseDTO;
import com.ms.Notification.Security.JwtUtil;
import com.ms.Notification.Service.AuditService;
import com.ms.Notification.Service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador para gestionar la bandeja de notificaciones.
 * Soporta la emisión de nuevas alertas internas, la lectura del buzón
 * y el reseteo del contador de mensajes no leídos del usuario logueado.
 */

@RestController
    @RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notificaciones", description = "Generación y consulta de alertas del sistema para los usuarios")
public class NotificationController {

    private final NotificationService notificationService;
    private final JwtUtil jwtUtil;
    private final AuditService auditoriaService;

    @PostMapping
    @Operation(summary = "Crear notificación", description = "Genera una alerta interna dirigida a un usuario")
    @ApiResponse(responseCode = "201", description = "Notificación enviada")
    public ResponseEntity<NotificationResponseDTO> createNotification(
            @Valid @RequestBody NotificationCreateDTO request,
            @RequestHeader(value = "X-Service-Origin", defaultValue = "SYSTEM") String serviceOrigin) {

        NotificationResponseDTO nuevaNotificacion = notificationService.createNotification(request, serviceOrigin);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaNotificacion);
    }

    @GetMapping("/mis-notificaciones")
    @Operation(summary = "Ver mis notificaciones", description = "Obtiene el historial de notificaciones del usuario")
    @ApiResponse(responseCode = "200", description = "Historial obtenido")
    public ResponseEntity<List<NotificationResponseDTO>> getMyNotifications(@RequestHeader("Authorization") String token) {
        Long myId = jwtUtil.extractUserId(token);
        return ResponseEntity.ok(notificationService.getUserNotifications(myId, token));
    }

    @GetMapping("/no-leidas")
    @Operation(summary = "Contador de no leídas", description = "Retorna el número exacto de notificaciones pendientes por leer")
    @ApiResponse(responseCode = "200", description = "Conteo calculado")
    public ResponseEntity<Map<String, Long>> getMyUnreadCount(@RequestHeader("Authorization") String token) {
        Long myId = jwtUtil.extractUserId(token);
        Long count = notificationService.getUnreadCount(myId);
        return ResponseEntity.ok(Map.of("UnreadCount", count));
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Marcar como leída", description = "Cambia el estado de una notificación específica a leída.")
    @ApiResponse(responseCode = "200", description = "Notificación actualizada")
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
