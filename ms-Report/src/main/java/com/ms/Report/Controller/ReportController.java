package com.ms.Report.Controller;

import com.ms.Report.Model.ReportCreateDTO;
import com.ms.Report.Model.ReportModel;
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

    @PostMapping("/create")
    public ResponseEntity<ReportModel> createReport(@Valid @RequestBody ReportCreateDTO request) {
        log.info("Peticion de reporte creada por el usuario: {}", request.getReporterUsername());

        ReportModel newReport = reportService.createReport(
                request.getReporterUsername(),
                request.getReportedEntityType(),
                request.getReportedEntityId(),
                request.getReason()
        );
        return new ResponseEntity<>(newReport, HttpStatus.CREATED);
    }

    @PutMapping("/{id}/resolve")
    public ResponseEntity<ReportModel> resolveReport(@PathVariable Long id) {
        log.info("Peticion para resolver el reporte con ID: {}", id);

        ReportModel resolvedReport = reportService.resolveReport(id);
        return ResponseEntity.ok(resolvedReport);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<ReportModel>> getsReportsByStatus(@PathVariable String status) {
        log.info("Buscando reportes con estado {}", status);

        List<ReportModel> reports = reportService.getReportsByStatus(status);
        return ResponseEntity.ok(reports);
    }

}