package com.ms.Report.Model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
