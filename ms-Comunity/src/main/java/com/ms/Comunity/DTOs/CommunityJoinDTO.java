package com.ms.Comunity.DTOs;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Credenciales para ingresar a una comunidad privada")
public class CommunityJoinDTO {

    @NotBlank(message = "El código de acceso es obligatorio")
    @Schema(description = "Código secreto de la comunidad a la que se desea ingresar", example = "java2026")
    private String accessCode;
}
