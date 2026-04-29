package com.ms.Comunity.Model;

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

    @NotBlank(message = "El nombre del creador es obligatorio")
    private String creatorUsername;
}
