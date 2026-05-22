package com.ms.Report.Service;

import com.ms.Report.Model.ReportModel;
import com.ms.Report.Repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;

    public ReportModel createReport(String reporterUsername, String reportedEntityType,
                                    Long reportedEntityId, String reason) {
        String typeUpper = reportedEntityType.toUpperCase();
        if (!typeUpper.equals("POST") && !typeUpper.equals("COMMENT") && !typeUpper.equals("USER")) {
            throw new IllegalArgumentException("El tipo de entidad reportada debe ser POST, COMMENT o USER");
        }

        ReportModel report = ReportModel.builder()
                .reporterUsername(reporterUsername)
                .reportedEntityType(typeUpper)
                .reportedEntityId(reportedEntityId)
                .reason(reason)
                .build();

        return reportRepository.save(report);
    }

    public ReportModel resolveReport(Long reportId) {
        ReportModel report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Ticket de reporte no encontrado"));

        report.setStatus("RESOLVED");
        return reportRepository.save(report);
    }

    public List<ReportModel> getReportsByStatus(String status) {
        return reportRepository.findByStatus(status.toUpperCase());
    }
}
