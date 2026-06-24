package com.ms.Messasing.DTOs;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageCreateDTO {

    @NotBlank(message = "El contenido no puede estar vacío")
    @Size(max = 500)
    private String contenido;

}
