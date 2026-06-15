package com.ms.Post.Model;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

@Schema(title = "DTO encargado de crear un post")

public class PostCreateDTO {

    @Schema(title= "Titulo del Post")

    @NotBlank(message = "El titulo es obligatorio")
    @Size(min = 5, max = 64, message = "El titulo debe tener entre 5 y 64 caracteres")
    private String titulo;

    @Schema(title = "contenido del post")

    @NotBlank(message = "El contenido no puede estar vacio")
    @Size(min = 10, max = 2000, message = "El contenido debe tener entre 10 y 2000 caracteres")
    private String contenido;

    private Long idComunidad;
}
