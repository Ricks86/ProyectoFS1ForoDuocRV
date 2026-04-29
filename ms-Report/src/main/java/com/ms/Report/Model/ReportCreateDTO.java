package com.ms.Report.Model;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReportCreateDTO {
    @NotBlank(message = "El usuario que reporta es obligatorio")
    private String reporterUsername;

    @NotBlank(message = "Debe especificar qué está reportando (POST, COMMENT, USER)")
    private String reportedEntityType;

    @NotNull(message = "Falta el ID del contenido reportado")
    private Long reportedEntityId;

    @NotBlank(message = "Debe incluir un motivo para el reporte")
    private String reason;
}
