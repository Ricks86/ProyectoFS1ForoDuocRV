package com.ms.Report.Model;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReportResponseDTO {
    private Long id;
    private String reporterUsername;
    private String reportedEntityType;
    private Long reportedEntityId;
    private String reason;
    private String status;
    private LocalDateTime createdAt;
}
