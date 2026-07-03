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

/**
 * Servicio central para la gestión de los reportes de moderación.
 * <p>
 * Se encarga de la persistencia de los tickets de reporte, la actualización de estados
 * y la integración síncrona con el microservicio ms-User para componer
 * la información del usuario denunciante en tiempo real mediante el patrón API Composition.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserClient userClient;

    /**
     * Crea un nuevo ticket de reporte validando el tipo de entidad permitida.
     *
     * @param request Objeto DTO con el tipo de entidad, ID y motivo del reporte.
     * @param reporterIdLogueado  Identificador del usuario que reporta, extraído del token JWT.
     * @param token Token de autorización para propagarlo hacia ms-User.
     * @return ReportResponseDTO con los datos guardados y el perfil del denunciante.
     */
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

    /**
     * Actualiza el estado de un reporte existente a RESOLVED.
     *
     * @param reportId Identificador único del reporte a resolver.
     * @param token Token de autorización para propagarlo hacia ms-User.
     * @return ReportResponseDTO actualizado.
     */
    @Transactional
    public ReportResponseDTO resolveReport(Long reportId, String token) {
        ReportModel report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Ticket de reporte no encontrado"));

        report.setStatus("RESOLVED");
        ReportModel updated = reportRepository.save(report);

        UserDTO reporter = obtenerUsuario(updated.getReporterId(), token);
        return construirResponseDTO(updated, reporter);
    }

    /**
     * Recupera una lista de reportes filtrados por su estado actual.
     * @param status Estado a buscar (ej. PENDING, RESOLVED).
     * @param token Token de autorización para propagarlo hacia ms-User.
     * @return Lista de ReportResponseDTO con la informaciòn de cada ticket.
     */
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

    /**
     * Resuelve los datos del usuario utilizando el Feign Client.
     * Proporciona un mecanismo de fallback generico en caso de que ms-User no este disponible.
     *
     * @param userId Identificador del usuario a consultar.
     * @param token  Token JWT para la autorización.
     * @return       UserDTO con la información recuperada o datos por defecto.
     */
    private UserDTO obtenerUsuario(Long userId, String token) {
        try {
            return userClient.obtenerUsuarioPorId(userId, token);
        } catch (Exception e) {
            log.warn("Fallo al contactar ms-User para reporte, ID {}: {}", userId, e.getMessage());
            return new UserDTO(userId, "Usuario Desconocido", "Alias No Disponible");
        }
    }

    /**
     * Mapea los datos del reporte junto con la información resuelta de su denunciante.
     *
     * @param model Entidad ReportModel con los detalles del reporte.
     * @param reporter DTO del usuario que generó el ticket.
     * @return ReportResponseDTO unificado.
     */
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
