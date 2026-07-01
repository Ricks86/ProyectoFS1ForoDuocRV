package com.ms.User.DTOs;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Datos permitidos para actualizar el perfil público del usuario")
public class UserUpdateDTO {

    @Size(max = 500, message = "La biografia no puede superar los 500 caracteres")
    @Schema(description = "Biografía o presentación del usuario", example = "Desarrollador java apasionado por el backend")
    private String bio;

    @Schema(description = "Enlace a la imagen de perfil del usuario", example = "https://ejemplo.com/avatares/mi-foto.png")
    private String avatarUrl;


}
