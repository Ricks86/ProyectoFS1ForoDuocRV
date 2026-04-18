package com.ms.Auth.Model;

import jakarta.validation.constraints.*;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequestDTO {

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 4, max = 32, message = "El usuario debe tener entre 4 y 32 caracteres")
    private String nombreUser;

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El correo electrónico no es válido")
    private String email;

    @NotBlank(message = "La contraseña es obligatorio")
    @Size(min = 8, message = "La contraseña debe tener al menos 8")
    private String password;
}
