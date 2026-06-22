package com.ms.Audit.Service;

import com.ms.Audit.Model.AuditModel;
import com.ms.Audit.Model.AuditRequestDTO;
import com.ms.Audit.Repository.AuditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditRepository auditRepository;

    @Transactional
    public void registrarAuditoria(AuditRequestDTO requestDTO) {
        try {
            AuditModel nuevoLog = AuditModel.builder()
                    .usuarioId(requestDTO.getUsuarioId())
                    .accion(requestDTO.getAccion())
                    .recurso(requestDTO.getRecurso())
                    .detalles(requestDTO.getDetalles())
                    .build();

            auditRepository.save(nuevoLog);

            log.info("Auditoría guardada: Acción [{}] en [{}] por Usuario ID [{}]",
                    nuevoLog.getAccion(), nuevoLog.getRecurso(), nuevoLog.getUsuarioId());

        } catch (Exception e) {
            log.error("Error crítico al persistir auditoría: {}", e.getMessage(), e);

            throw new RuntimeException("Fallo al guardar el registro de auditoría", e);
        }
    }
}
