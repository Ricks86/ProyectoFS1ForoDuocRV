package com.ms.Comunity.DTOs;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Estructura para inicializar una nueva comunidad")
public class CommunityCreateDTO {
    @NotBlank(message = "El nombre de la comunidad es obligatorio")
    @Size(min = 3, max = 50)
    @Schema(description = "Nombre oficial de la comunidad", example = "Java Fans")
    private String name;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 500)
    @Schema(description = "Descripción y reglas de la comunidad", example = "Foro dedicado al ecosistema spring y java.")
    private String description;

    @NotBlank(message = "El codigo de acceso es obligatorio")
    @Schema(description = "Contraseña compartida para nuevos miembros", example = "java2026")
    private String communityAccess;
}
