package com.ms.Notification.DTOs;

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
@Schema(description = "Datos para generar una nueva alerta o notificación")
public class    NotificationCreateDTO {

    @NotNull(message = "El destinatario es obligatorio")
    @Schema(description = "ID del usuario que recibirá la notificación", example = "5")
    private Long recipientId;

    @Schema(description = "ID del usuario que originó la acción ", example = "12")
    private Long senderId;

    @NotBlank(message = "El tipo es obligatorio")
    @Schema(description = "Categoría de la notificación", example = "LIKE")
    private String Type;

    @NotBlank(message = "El mensaje es obligatorio")
    @Schema(description = "Cuerpo del mensaje", example = "A un usuario le ha gustado tu publicación.")
    private String message;

    @Schema(description = "ID del post o comentario relacionado", example = "105")
    private Long relatedId;
}
