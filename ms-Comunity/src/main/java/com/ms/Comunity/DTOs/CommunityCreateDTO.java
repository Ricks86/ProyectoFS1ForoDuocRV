package com.ms.Comunity.DTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class CommunityCreateDTO {
    @NotBlank(message = "El nombre de la comunidad es obligatorio")
    @Size(min = 3, max = 50)
    private String name;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 500)
    private String description;

    @NotBlank(message = "El codigo de acceso es obligatorio")
    private String communityAccess;
}
