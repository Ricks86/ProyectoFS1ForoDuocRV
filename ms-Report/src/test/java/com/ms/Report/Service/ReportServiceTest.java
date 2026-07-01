package com.ms.Report.Service;

import com.ms.Report.Client.UserClient;
import com.ms.Report.DTOs.ReportCreateDTO;
import com.ms.Report.DTOs.ReportResponseDTO;
import com.ms.Report.DTOs.UserDTO;
import com.ms.Report.Model.ReportModel;
import com.ms.Report.Repository.ReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;


@ExtendWith(MockitoExtension.class)
public class ReportServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private UserClient userClient;

    @InjectMocks
    private ReportService reportService;

    private ReportCreateDTO createRequest;
    private ReportModel mockReport;
    private UserDTO mockUserDTO;
    private final String MOCK_TOKEN = "Bearer fake-token-123";

    @BeforeEach
    void setUp() {
        createRequest = new ReportCreateDTO();
        createRequest.setReportedEntityType("POST");
        createRequest.setReportedEntityId(50L);
        createRequest.setReason("Contenido inapropiado");

        mockReport = ReportModel.builder()
                .id(1L)
                .reporterId(100L)
                .reportedEntityType("POST")
                .reportedEntityId(50L)
                .reason("Contenido inapropiado")
                .status("PENDING")
                .build();

        mockUserDTO = new UserDTO(100L, "usuarioTest", "Alias Test");
    }

    @Test
    void testCreateReport_Exito() {
        Mockito.when(reportRepository.save(any(ReportModel.class))).thenReturn(mockReport);
        Mockito.when(userClient.obtenerUsuarioPorId(eq(100L), eq(MOCK_TOKEN))).thenReturn(mockUserDTO);

        ReportResponseDTO response = reportService.createReport(createRequest, 100L, MOCK_TOKEN);

        assertNotNull(response);
        assertEquals("PENDING", response.getStatus());
        assertEquals("usuarioTest", response.getReporter().getUsername());
        Mockito.verify(reportRepository, Mockito.times(1)).save(any(ReportModel.class));
    }

    @Test
    void testCreateReport_FallaEntidadInvalida() {
        createRequest.setReportedEntityType("INVALIDO");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reportService.createReport(createRequest, 100L, MOCK_TOKEN);
        });

        assertEquals("El tipo de entidad reportada debe ser POST, COMMENT o USER", exception.getMessage());
        Mockito.verify(reportRepository, Mockito.never()).save(any(ReportModel.class));
    }

    @Test
    void testCreateReport_FallaUserClient_FallbackAsignado() {
        Mockito.when(reportRepository.save(any(ReportModel.class))).thenReturn(mockReport);
        Mockito.when(userClient.obtenerUsuarioPorId(eq(100L), eq(MOCK_TOKEN)))
                .thenThrow(new RuntimeException("ms-User no disponible"));

        ReportResponseDTO response = reportService.createReport(createRequest, 100L, MOCK_TOKEN);

        assertNotNull(response);
        assertEquals("Usuario Desconocido", response.getReporter().getUsername());
        assertEquals("Alias No Disponible", response.getReporter().getAlias());
    }

    @Test
    void testResolveReport_Exito() {
        Mockito.when(reportRepository.findById(1L)).thenReturn(Optional.of(mockReport));
        Mockito.when(reportRepository.save(any(ReportModel.class))).thenReturn(mockReport);
        Mockito.when(userClient.obtenerUsuarioPorId(eq(100L), eq(MOCK_TOKEN))).thenReturn(mockUserDTO);

        ReportResponseDTO response = reportService.resolveReport(1L, MOCK_TOKEN);

        assertNotNull(response);
        assertEquals("RESOLVED", mockReport.getStatus());
        Mockito.verify(reportRepository, Mockito.times(1)).save(any(ReportModel.class));
    }

    @Test
    void testResolveReport_NoEncontrado() {
        Mockito.when(reportRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            reportService.resolveReport(99L, MOCK_TOKEN);
        });

        assertEquals("Ticket de reporte no encontrado", exception.getMessage());
        Mockito.verify(reportRepository, Mockito.never()).save(any(ReportModel.class));
    }

    @Test
    void testGetReportsByStatus_Exito() {
        Mockito.when(reportRepository.findByStatus("PENDING")).thenReturn(List.of(mockReport));
        Mockito.when(userClient.obtenerUsuarioPorId(eq(100L), eq(MOCK_TOKEN))).thenReturn(mockUserDTO);

        List<ReportResponseDTO> responses = reportService.getReportsByStatus("pending", MOCK_TOKEN);

        assertFalse(responses.isEmpty());
        assertEquals(1, responses.size());
        assertEquals("POST", responses.get(0).getReportedEntityType());
        assertEquals("usuarioTest", responses.get(0).getReporter().getUsername());
    }
}