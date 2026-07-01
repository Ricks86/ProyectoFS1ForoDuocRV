package com.ms.Report.DTOs;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Datos necesarios para generar un nuevo reporte")
public class ReportCreateDTO {

    @NotBlank(message = "Debe especificar qué está reportando (POST, COMMENT, USER)")
    @Schema(description = "Tipo de entidad que se esta reportando", example = "POST")
    private String reportedEntityType;

    @NotNull(message = "Falta el ID del contenido reportado")
    @Schema(description = "ID único del contenido o usuario reportado", example = "42")
    private Long reportedEntityId;

    @NotBlank(message = "Debe incluir un motivo para el reporte")
    @Schema(description = "Motivo detallado del reporte", example = "Contenido inapropiado u ofensivo")
    private String reason;
}
