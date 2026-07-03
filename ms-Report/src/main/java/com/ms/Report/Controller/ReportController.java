package com.ms.Report.Controller;

import com.ms.Report.DTOs.ReportCreateDTO;
import com.ms.Report.DTOs.ReportResponseDTO;
import com.ms.Report.Security.JwtUtil;
import com.ms.Report.Service.AuditService;
import com.ms.Report.Service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador que expone la API para el sistema de moderación.
 * Permite a los usuarios denunciar contenido (posts, comentarios) y a los
 * moderadores revisar y dar por resueltos los tickets.
 *
 * NOTA: Los detalles exactos de los payloads y respuestas están en Swagger.
 */

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Reportes", description = "Gestion de tickets de moderacion y reportes de usuarios")
public class ReportController {

    private final ReportService reportService;
    private final JwtUtil jwtUtil;
    private final AuditService auditoriaService;

    @PostMapping("/create")
    @Operation(summary = "Crear un reporte", description = "Genera un nuevo ticket de reporte ")
    @ApiResponse(responseCode = "201", description = "Reporte creado exitosamente")
    public ResponseEntity<ReportResponseDTO> createReport(
            @Valid @RequestBody ReportCreateDTO request,
            @RequestHeader("Authorization") String token) {

        Long reporterIdLogueado = jwtUtil.extractUserId(token);
        log.info("Peticion de reporte creada por el ID: {}", reporterIdLogueado);

        ReportResponseDTO newReport = reportService.createReport(request, reporterIdLogueado, token);

        auditoriaService.registrarLog(
                reporterIdLogueado,
                "CREATE_REPORT",
                "Reporte creado contra entidad [" + request.getReportedEntityType() + "] con ID [" + request.getReportedEntityId() + "]"
        );

        return new ResponseEntity<>(newReport, HttpStatus.CREATED);
    }

    @PutMapping("/{id}/resolve")
    @Operation(summary = "Resolver reporte", description = "Marca un ticket de reporte específico como resuelto")
    @ApiResponse(responseCode = "200", description = "Reporte resuelto correctamente")
    public ResponseEntity<ReportResponseDTO> resolveReport(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {

        Long moderadorId = jwtUtil.extractUserId(token);
        log.info("Peticion para resolver el reporte con ID: {} por moderador: {}", id, moderadorId);

        ReportResponseDTO resolvedReport = reportService.resolveReport(id, token);

        auditoriaService.registrarLog(
                moderadorId,
                "RESOLVE_REPORT",
                "El ticket de reporte ID [" + id + "] ha sido marcado como RESOLVED"
        );

        return ResponseEntity.ok(resolvedReport);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Filtrar reportes por estado", description = "Obtiene una lista de reportes según su estado")
    @ApiResponse(responseCode = "200", description = "Lista de reportes obtenida")
    public ResponseEntity<List<ReportResponseDTO>> getsReportsByStatus(
            @PathVariable String status,
            @RequestHeader("Authorization") String token) {

        log.info("Buscando reportes con estado {}", status);
        List<ReportResponseDTO> reports = reportService.getReportsByStatus(status, token);
        return ResponseEntity.ok(reports);
    }



}