package com.ms.Comunity.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "communities")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommunityModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre de la comunidad es obligatorio")
    @Size(min = 3, max = 50, message = "El nombre debe tener entre 3 y 50 caracteres")
    @Column(unique = true, nullable = false)
    private String name; // Ej: "r/tecnologia"

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
    @Column(nullable = false, length = 500)
    private String description;

    @NotBlank(message = "El creador es obligatorio")
    @Column(nullable = false)
    private String creatorUsername;

    @NotBlank(message = "El codigo de acceso es obligatorio")
    private String accessCode;

    @ElementCollection
    @CollectionTable(name = "community_members", joinColumns = @JoinColumn(name = "community_id"))
    @Column(name = "user_id")
    @Builder.Default
    private List<Long> memberIds = new ArrayList<>();

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    private Integer memberCount = 0;
}
