package com.ms.Interaction.Service;

import com.ms.Interaction.Client.AuditClient;
import com.ms.Interaction.DTOs.AuditRequestDTO;
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
                    "ms-Interaction",
                    detalles
            );

            auditClient.registrarAccion(auditoria);
            log.info("Auditoría enviada asíncronamente: {}", accion);

        } catch (Exception e) {
            log.error("Fallo al contactar ms-Comment (El sistema sigue funcionando): {}", e.getMessage());
        }
    }
}
