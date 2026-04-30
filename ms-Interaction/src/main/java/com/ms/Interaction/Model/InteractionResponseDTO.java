package com.ms.Interaction.Model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InteractionResponseDTO {
    private Long id;
    private String username;
    private String entityType;
    private Long entityId;
    private String voteType;
    private LocalDateTime createdAt;
}
