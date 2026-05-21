package com.ms.Messasing.Model;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageCreatetDTO {

    @NotBlank(message = "El contenido no puede estar vacío")
    @Size(max = 500)
    private String contenido;

}
