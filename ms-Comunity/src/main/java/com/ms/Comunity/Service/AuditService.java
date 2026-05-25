package com.ms.Comunity.Service;

import com.ms.Comunity.Client.AuditClient;
import com.ms.Comunity.Model.AuditRequestDTO;
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
                    "ms-Comunity",
                    detalles
            );

            auditClient.registrarAccion(auditoria);
            log.info("Auditoría enviada asíncronamente: {}", accion);

        } catch (Exception e) {
            log.error("Fallo al contactar ms-Comment (El sistema sigue funcionando): {}", e.getMessage());
        }
    }
}
