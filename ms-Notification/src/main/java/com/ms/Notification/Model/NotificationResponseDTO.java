package com.ms.Notification.Model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationResponseDTO {

    private Long id;
    private String senderUsername;
    private String type;
    private String message;
    private Long relatedId;
    private Boolean isRead;
    private LocalDateTime createdAt;
}
