package com.ms.Report.Model;


import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El usuario que reporta es obligatorio")
    @Column(nullable = false)
    private String reporterUsername;

    @NotBlank(message = "El tipo de entidad reportada es obligatorio (POST, COMMENT, USER)")
    @Column(nullable = false)
    private String reportedEntityType;

    @NotNull(message = "El ID de la entidad reportada es obligatorio")
    @Column(nullable = false)
    private Long reportedEntityId;

    @NotBlank(message = "El motivo del reporte es obligatorio")
    @Column(nullable = false, length = 500)
    private String reason;

    @Builder.Default
    @Column(nullable = false)
    private String status = "PENDING";

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
