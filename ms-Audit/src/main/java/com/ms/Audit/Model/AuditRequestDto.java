package com.ms.Audit.Model;

import jakarta.validation.constraints.NotBlank;

public record AuditRequestDto(
        @NotBlank(message = "El nombre del servicio es obligatiorio")
        String nombreService,
        @NotBlank(message = "La accion es obligatoria")
        String accion,
        String userID,
        String details
) {}
