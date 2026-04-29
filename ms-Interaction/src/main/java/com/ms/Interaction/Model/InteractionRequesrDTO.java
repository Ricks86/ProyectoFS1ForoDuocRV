package com.ms.Interaction.Model;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InteractionRequesrDTO {
    @NotBlank(message = "El nombre de usuario es obligatorio")
    private String username;

    @NotBlank(message = "Especifique POST o COMMENT")
    private String entityType;

    @NotNull(message = "El ID del post/comentario es obligatorio")
    private Long entityId;

    @NotBlank(message = "Especifique UPVOTE o DOWNVOTE")
    private String voteType;
}
