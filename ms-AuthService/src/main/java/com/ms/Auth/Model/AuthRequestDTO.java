package com.ms.Auth.Model;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthRequestDTO {

    @NotBlank(message = "El nombre de usuario es obligatorio")
    private String nombreUser;

    @NotBlank(message = "La contraseña es obligatorio")
    private String password;
}
