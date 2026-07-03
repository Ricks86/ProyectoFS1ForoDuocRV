package com.ms.Audit.Service;

import com.ms.Audit.Model.AuditModel;
import com.ms.Audit.DTOs.AuditRequestDTO;
import com.ms.Audit.Repository.AuditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio centralizado para Ms Audit.
 * <p>
 * Actúa como el Log de eventos del ecosistema de microservicios,
 * garantizando el registro inmutable de acciones críticas (creación de cuentas,
 * publicaciones, envío de mensajes, etc.) para fines de monitoreo y trazabilidad.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditRepository auditRepository;

    /**
     * Registra un nuevo evento de auditoría en la base de datos.
     * <p>
     * Este método asegura la escritura del log.
     * Si ocurre un error a nivel de infraestructura (ej. base de datos inaccesible),
     * captura la excepción para imprimir el stacktrace localmente y propaga una
     * RuntimeException. Esto informa explícitamente a los microservicios clientes
     * que la trazabilidad ha fallado.
     *
     * @param requestDTO Objeto DTO que encapsula el ID del usuario, la acción realizada, el recurso afectado y el detalle del evento.
     * @throws RuntimeException Si el motor de base de datos rechaza la inserción o se encuentra fuera de línea.
     */
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
