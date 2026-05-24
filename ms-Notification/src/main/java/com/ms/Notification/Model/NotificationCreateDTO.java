package com.ms.Notification.Model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class    NotificationCreateDTO {

    @NotBlank(message = "El destinatario es obligatorio")
    private Long recipientId;

    private Long senderId;

    @NotBlank(message = "El tipo es obligatorio")
    private String Type;

    @NotBlank(message = "El mensaje es obligatorio")
    private String message;

    private Long relatedId;
}
