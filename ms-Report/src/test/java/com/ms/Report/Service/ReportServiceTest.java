package com.ms.Report.Service;

import com.ms.Report.Client.UserClient;
import com.ms.Report.DTOs.ReportCreateDTO;
import com.ms.Report.DTOs.ReportResponseDTO;
import com.ms.Report.Model.ReportModel;
import com.ms.Report.Repository.ReportRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
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

    @Test
    void testCreateReport() {
        ReportCreateDTO req = new ReportCreateDTO();
        req.setReportedEntityType("POST");
        req.setReportedEntityId(1L);
        req.setReason("Motivo de prueba");

        ReportModel saved = new ReportModel();
        saved.setId(10L);
        saved.setReportedEntityType("POST");
        saved.setStatus("PENDING");

        Mockito.when(reportRepository.save(any(ReportModel.class))).thenReturn(saved);

        ReportResponseDTO result = reportService.createReport(req, 1L, "token");

        assertNotNull(result);
        assertEquals("PENDING", result.getStatus());
    }

    @Test
    void testResolveReport() {
        ReportModel report = new ReportModel();
        report.setId(10L);
        report.setStatus("PENDING");

        Mockito.when(reportRepository.findById(anyLong())).thenReturn(Optional.of(report));
        Mockito.when(reportRepository.save(any(ReportModel.class))).thenReturn(report);

        ReportResponseDTO result = reportService.resolveReport(10L, "token");

        assertNotNull(result);
        assertEquals("RESOLVED", report.getStatus());
    }

    @Test
    void testGetReportsByStatus() {
        Mockito.when(reportRepository.findByStatus(anyString())).thenReturn(Collections.emptyList());

        List<ReportResponseDTO> result = reportService.getReportsByStatus("PENDING", "token");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }


}
