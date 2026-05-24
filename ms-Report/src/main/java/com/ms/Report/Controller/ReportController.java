package com.ms.Report.Controller;

import com.ms.Report.Model.ReportCreateDTO;
import com.ms.Report.Model.ReportModel;
import com.ms.Report.Model.ReportResponseDTO;
import com.ms.Report.Security.JwtUtil;
import com.ms.Report.Service.AuditService;
import com.ms.Report.Service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Slf4j
public class ReportController {

    private final ReportService reportService;
    private final JwtUtil jwtUtil;
    private final AuditService auditoriaService;

    @PostMapping("/create")
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
    public ResponseEntity<List<ReportResponseDTO>> getsReportsByStatus(
            @PathVariable String status,
            @RequestHeader("Authorization") String token) {

        log.info("Buscando reportes con estado {}", status);
        List<ReportResponseDTO> reports = reportService.getReportsByStatus(status, token);
        return ResponseEntity.ok(reports);
    }



}