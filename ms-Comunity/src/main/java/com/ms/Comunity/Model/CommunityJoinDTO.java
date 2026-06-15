package com.ms.Comunity.Model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CommunityJoinDTO {

    @NotBlank(message = "El código de acceso es obligatorio")
    private String accessCode;
}
