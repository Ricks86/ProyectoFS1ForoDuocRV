package com.ms.User.DTOs;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInitDTO {
    @NotNull(message = "El authId es obligatorio")
    private Long authId;

    @NotBlank(message = "El username es obligatorio")
    private String username;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Debe ser un formato de correo válido")
    private String email;
}
