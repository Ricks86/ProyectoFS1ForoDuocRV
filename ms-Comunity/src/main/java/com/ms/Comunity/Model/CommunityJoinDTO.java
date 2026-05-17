package com.ms.Comunity.Model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommunityJoinDTO {
    @NotNull(message = "El ID del usuario es obligatorio")
    private Long userId;

    @NotBlank(message = "El código de acceso es obligatorio")
    private String accessCode;
}
