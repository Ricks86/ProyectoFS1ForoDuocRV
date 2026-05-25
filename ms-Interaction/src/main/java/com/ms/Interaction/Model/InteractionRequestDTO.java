package com.ms.Interaction.Model;

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
public class InteractionRequestDTO {
    @NotBlank(message = "El tipo de entidad es obligatorio (POST, COMMENT)")
    private String entityType;

    @NotNull(message = "El ID de la entidad es obligatorio")
    private Long entityId;

    @NotBlank(message = "El tipo de voto es obligatorio (UPVOTE, DOWNVOTE)")
    private String voteType;
}
