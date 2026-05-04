package com.ms.Audit.Controller;

import com.ms.Audit.Model.AuditModel;
import com.ms.Audit.Model.AuditRequestDto;
import com.ms.Audit.Service.AuditService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;


@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
@Validated
public class AuditController {
    private final AuditService service;

    @PostMapping
    public ResponseEntity<String> registrarActividad(@Valid @RequestBody AuditRequestDto request){
        service.crearLog(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("log registrado exitosamente");
    }

    @GetMapping
    public ResponseEntity<List<AuditModel>> obtenerTodos() {
        return ResponseEntity.ok(service.getAllLog());
    }

    @GetMapping("/service/{name}")
    public ResponseEntity<List<AuditModel>> logsPorServicio(@PathVariable String servicio) {
        List<AuditModel> logs = service.getLogsPorNombre(servicio);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/filter")
    public ResponseEntity<List<AuditModel>> logsPorFecha(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(service.getLogsPorRangoFecha(start, end));
    }

 }
