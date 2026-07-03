package com.ms.Audit.Controller;

import com.ms.Audit.DTOs.AuditRequestDTO;
import com.ms.Audit.Service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador REST para MS Audit.
 * <p>
 * Este componente opera primariamente como una interfaz de comunicación interna
 * dentro de la arquitectura. Recibe los eventos de trazabilidad disparados por
 * el resto de los microservicios y delega su persistencia a la capa de Servicio.
 * <p>
 * NOTA: La especificación técnica de los endpoints, estructuras JSON (DTOs)
 * y mapas de códigos de estado HTTP se encuentra expuesta exclusivamente a través de Swagger UI.
 */
@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Auditoría",
        description = "Operaciones internas para el registro y trazabilidad de eventos de seguridad en el ecosistema")
public class AuditController {
    private final AuditService auditService;

    @PostMapping
    @Operation(
            summary = "Registrar nuevo log de auditoría",
            description = "Endpoint diseñado para ser consumido por otros microservicios (vía OpenFeign). Persiste una acción de negocio, el recurso afectado y el ID del usuario responsable."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Registro de auditoría persistido exitosamente en el sumidero."),
            @ApiResponse(responseCode = "400", description = "Estructura del log mal formada o DTO incompleto."),
            @ApiResponse(responseCode = "500", description = "Error crítico en el motor de base de datos local.")
    })
    public ResponseEntity<Map<String, String>> registrarAuditoria(@Valid @RequestBody AuditRequestDTO requestDTO) {

        auditService.registrarAuditoria(requestDTO);

        Map<String, String> respuesta = Map.of(
                "mensaje", "Registro de auditoría guardado correctamente",
                "estado", "EXITOSO"
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }
 }
