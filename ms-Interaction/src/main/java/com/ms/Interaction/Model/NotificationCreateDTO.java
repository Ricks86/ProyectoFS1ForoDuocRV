package com.ms.Interaction.Model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationCreateDTO {
    private Long recipientId;
    private Long senderId;
    private String type;
    private String message;
    private Long relatedId;
}
