package com.ms.Auth.DTOs;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequestDTO {

    @NotBlank(message = "El nombre de usuario es obligatorio")
    private String nombreUser;

    @NotBlank(message = "La contraseña es obligatorio")
    private String password;
}
