package com.ms.Interaction.DTOs;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Estructura para enviar o modificar un voto")
public class InteractionRequestDTO {

    @NotBlank(message = "El tipo de entidad es obligatorio (POST, COMMENT)")
    @Schema(description = "Tipo de la entidad a votar", example = "POST")
    private String entityType;

    @NotNull(message = "El ID de la entidad es obligatorio")
    @Schema(description = "ID de la publicacion o comentario", example = "10")
    private Long entityId;

    @NotBlank(message = "El tipo de voto es obligatorio (UPVOTE, DOWNVOTE)")
    @Schema(description = "Tipo de acción a realizar", example = "UPVOTE")
    private String voteType;
}
