package com.ms.Report.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ms.Report.DTOs.ReportCreateDTO;
import com.ms.Report.DTOs.ReportResponseDTO;
import com.ms.Report.Security.JwtUtil;
import com.ms.Report.Service.AuditService;
import com.ms.Report.Service.ReportService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReportController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    ReportService reportService;

    @MockBean
    JwtUtil jwtUtil;

    @MockBean
    AuditService auditoriaService;

    @Test
    void testCreateReport() throws Exception {
        ReportCreateDTO request = ReportCreateDTO.builder()
                .reportedEntityType("POST")
                .reportedEntityId(10L)
                .reason("Contenido inapropiado u ofensivo")
                .build();

        ReportResponseDTO response = new ReportResponseDTO();

        Mockito.when(jwtUtil.extractUserId(anyString())).thenReturn(1L);
        Mockito.when(reportService.createReport(any(ReportCreateDTO.class), any(Long.class), anyString()))
                .thenReturn(response);

        mockMvc.perform(post("/api/reports/create")
                .header("Authorization", "Bearer token-falso")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    void toResolveReport() throws Exception {
        ReportResponseDTO response = new ReportResponseDTO();

        Mockito.when(jwtUtil.extractUserId(anyString())).thenReturn(2L);
        Mockito.when(reportService.resolveReport(any(Long.class), anyString()))
                .thenReturn(response);

        mockMvc.perform(put("/api/reports/1/resolve")
                .header("Authorization", "Bearer token-falso"))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    void toGetsReport() throws Exception {
        Mockito.when(reportService.getReportsByStatus(anyString(), anyString()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/reports/status/PENDING")
                .header("Authorization", "Bearer token-falso"))
                .andExpect(status().is2xxSuccessful());
    }
}
