package com.ms.Report.Model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReportCreateDTO {

    @NotBlank(message = "Debe especificar qué está reportando (POST, COMMENT, USER)")
    private String reportedEntityType;

    @NotNull(message = "Falta el ID del contenido reportado")
    private Long reportedEntityId;

    @NotBlank(message = "Debe incluir un motivo para el reporte")
    private String reason;
}
