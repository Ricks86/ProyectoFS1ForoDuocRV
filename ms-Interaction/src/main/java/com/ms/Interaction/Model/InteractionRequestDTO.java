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
    @NotBlank(message = "El nombre de usuario es obligatorio")
    private String username;

    @NotBlank(message = "Especifique POST o COMMENT")
    private String entityType;

    @NotNull(message = "El ID del post/comentario es obligatorio")
    private Long entityId;

    @NotBlank(message = "Especifique UPVOTE o DOWNVOTE")
    private String voteType;
}
