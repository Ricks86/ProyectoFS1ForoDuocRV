package com.ms.Audit.Controller;

import com.ms.Audit.Model.AuditRequestDTO;
import com.ms.Audit.Service.AuditService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditController {
    private final AuditService auditService;

    @PostMapping
    public ResponseEntity<Map<String, String>> registrarAuditoria(@Valid @RequestBody AuditRequestDTO requestDTO) {

        auditService.registrarAuditoria(requestDTO);

        Map<String, String> respuesta = Map.of(
                "mensaje", "Registro de auditoría guardado correctamente",
                "estado", "EXITOSO"
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }



 }
