package com.ms.Post.Model;


import jakarta.validation.constraints.*;
import lombok.*;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostCreateDTO {

    @NotBlank(message = "El titulo es obligatorio")
    @Size(min = 5, max = 64, message = "El titulo debe tener entre 5 y 64 caracteres")
    private String titulo;

    @NotBlank(message = "El contenido no puede estar vacio")
    @Size(min = 10, max = 2000, message = "El contenido debe tener entre 10 y 2000 caracteres")
    private String contenido;

    @NotNull(message = "El ID de comunidad es obligatorio")
    private Long idComunidad;
}
