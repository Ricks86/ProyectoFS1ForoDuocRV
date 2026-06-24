package com.ms.Report.Service;

import com.ms.Report.Client.UserClient;
import com.ms.Report.DTOs.ReportCreateDTO;
import com.ms.Report.Model.ReportModel;
import com.ms.Report.DTOs.ReportResponseDTO;
import com.ms.Report.DTOs.UserDTO;
import com.ms.Report.Repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserClient userClient;

    @Transactional
    public ReportResponseDTO createReport(ReportCreateDTO request, Long reporterIdLogueado, String token) {
        String typeUpper = request.getReportedEntityType().toUpperCase();
        if (!typeUpper.equals("POST") && !typeUpper.equals("COMMENT") && !typeUpper.equals("USER")) {
            throw new IllegalArgumentException("El tipo de entidad reportada debe ser POST, COMMENT o USER");
        }

        ReportModel report = ReportModel.builder()
                .reporterId(reporterIdLogueado)
                .reportedEntityType(typeUpper)
                .reportedEntityId(request.getReportedEntityId())
                .reason(request.getReason())
                .status("PENDING")
                .build();

        ReportModel saved = reportRepository.save(report);
        UserDTO reporter = obtenerUsuario(reporterIdLogueado, token);

        return construirResponseDTO(saved, reporter);
    }

    @Transactional
    public ReportResponseDTO resolveReport(Long reportId, String token) {
        ReportModel report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Ticket de reporte no encontrado"));

        report.setStatus("RESOLVED");
        ReportModel updated = reportRepository.save(report);

        UserDTO reporter = obtenerUsuario(updated.getReporterId(), token);
        return construirResponseDTO(updated, reporter);
    }

    @Transactional(readOnly = true)
    public List<ReportResponseDTO> getReportsByStatus(String status, String token) {
        List<ReportModel> reports = reportRepository.findByStatus(status.toUpperCase());

        Map<Long, UserDTO> userCache = new HashMap<>();

        return reports.stream()
                .map(report -> {
                    UserDTO reporterDto = userCache.computeIfAbsent(report.getReporterId(),
                            id -> obtenerUsuario(id, token));
                    return construirResponseDTO(report, reporterDto);
                })
                .toList();
    }

    private UserDTO obtenerUsuario(Long userId, String token) {
        try {
            return userClient.obtenerUsuarioPorId(userId, token);
        } catch (Exception e) {
            log.warn("Fallo al contactar ms-User para reporte, ID {}: {}", userId, e.getMessage());
            return new UserDTO(userId, "Usuario Desconocido", "Alias No Disponible");
        }
    }

    private ReportResponseDTO construirResponseDTO(ReportModel model, UserDTO reporter) {
        return ReportResponseDTO.builder()
                .id(model.getId())
                .reporter(reporter)
                .reportedEntityType(model.getReportedEntityType())
                .reportedEntityId(model.getReportedEntityId())
                .reason(model.getReason())
                .status(model.getStatus())
                .build();
    }
}
