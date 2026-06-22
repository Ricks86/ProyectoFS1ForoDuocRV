package com.ms.Audit.Service;

import com.ms.Audit.Model.AuditModel;
import com.ms.Audit.Model.AuditRequestDTO;
import com.ms.Audit.Repository.AuditRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class AuditServiceTest {
    @Mock
    private AuditRepository auditRepository;

    @InjectMocks
    private AuditService auditService;

    private AuditRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        requestDTO = new AuditRequestDTO();
        requestDTO.setUsuarioId(1L);
        requestDTO.setAccion("TEST_ACTION");
        requestDTO.setRecurso("ms-Test");
        requestDTO.setDetalles("Prueba de auditoría");
    }

    @Test
    void testRegistrarAuditoria_OK() {
        Mockito.when(auditRepository.save(any(AuditModel.class))).thenReturn(new AuditModel());

        assertDoesNotThrow(() -> auditService.registrarAuditoria(requestDTO));

        Mockito.verify(auditRepository, Mockito.times(1)).save(any(AuditModel.class));
    }

    @Test
    void testRegistrarAuditoria_500() {
        Mockito.doThrow(new RuntimeException("Base de datos caída"))
                .when(auditRepository).save(any(AuditModel.class));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            auditService.registrarAuditoria(requestDTO);
        });

        assertTrue(exception.getMessage().contains("Fallo al guardar el registro de auditoría"));
    }
}