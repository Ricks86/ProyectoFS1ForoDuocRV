package com.ms.Notification.Service;

import com.ms.Notification.Client.AuditClient;
import com.ms.Notification.DTOs.AuditRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {
    private final AuditClient auditClient;

    @Async
    public void registrarLog(Long usuarioId, String accion, String detalles) {
        try {
            AuditRequestDTO auditoria = new AuditRequestDTO(
                    usuarioId,
                    accion,
                    "ms-Notification",
                    detalles
            );

            auditClient.registrarAccion(auditoria);
            log.info("Auditoría enviada asíncronamente: {}", accion);

        } catch (Exception e) {
            log.error("Fallo al contactar ms-Messagin (El sistema sigue funcionando): {}", e.getMessage());
        }
    }
}
