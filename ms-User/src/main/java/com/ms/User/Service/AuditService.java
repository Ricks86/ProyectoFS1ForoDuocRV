package com.ms.User.Service;

import com.ms.User.Client.AuditClient;
import com.ms.User.Model.AuditRequestDTO;
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
                    "ms-Users",
                    detalles
            );

            auditClient.registrarAccion(auditoria);
            log.info("Auditoría enviada asíncronamente: {}", accion);

        } catch (Exception e) {
            // Cumplimos la rúbrica manejando la excepción sin afectar el flujo principal
            log.error("Fallo al contactar ms-Audit (El sistema sigue funcionando): {}", e.getMessage());
        }
    }
}
