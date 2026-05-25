package com.ms.Interaction.Model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InteractionResponseDTO {
    private String status;
    private String entityType;
    private Long entityId;
    private String voteType;
    private UserDTO user;
}
