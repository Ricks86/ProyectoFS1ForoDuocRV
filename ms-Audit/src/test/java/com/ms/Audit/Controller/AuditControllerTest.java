package com.ms.Audit.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ms.Audit.Model.AuditRequestDTO;
import com.ms.Audit.Service.AuditService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuditController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuditControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuditService auditService;

    @Test
    void testRegistrarAuditoria_ExitoController() throws Exception {
        AuditRequestDTO requestDTO = new AuditRequestDTO();
        requestDTO.setUsuarioId(1L);
        requestDTO.setAccion("TEST_ACTION");
        requestDTO.setRecurso("ms-Test");
        requestDTO.setDetalles("Detalles de prueba para el controlador");

        Mockito.doNothing().when(auditService).registrarAuditoria(any(AuditRequestDTO.class));

        mockMvc.perform(post("/api/audit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mensaje").value("Registro de auditoría guardado correctamente"))
                .andExpect(jsonPath("$.estado").value("EXITOSO"));

        Mockito.verify(auditService, Mockito.times(1)).registrarAuditoria(any(AuditRequestDTO.class));
    }
}