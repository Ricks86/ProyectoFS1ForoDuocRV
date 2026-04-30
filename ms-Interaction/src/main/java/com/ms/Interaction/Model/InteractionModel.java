package com.ms.Interaction.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "votes", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"username", "entityType", "entityId"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class InteractionModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El usuario es obligatorio")
    @Column(nullable = false)
    private String username;

    @NotBlank(message = "El tipo de entidad es obligatorio (POST o COMMENT)")
    @Column(nullable = false)
    private String entityType;

    @NotNull(message = "El ID de la entidad es obligatorio")
    @Column(nullable = false)
    private Long entityId;

    // Valores: "UPVOTE" (1) o "DOWNVOTE" (-1)
    @NotBlank(message = "El tipo de voto es obligatorio")
    @Column(nullable = false)
    private String voteType;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
