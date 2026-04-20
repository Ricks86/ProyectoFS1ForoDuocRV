package com.ms.Messasing.Model;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageRequestDTO {

    @NotBlank(message = "El contenido no puede estar vacío")
    @Size(max = 500)
    private String contenido;

    @NotNull(message = "El ID del emisor es obligatorio")
    private Long emisorId;

    @NotNull(message = "El ID del receptor es obligatorio")
    private Long receptorId;
}
