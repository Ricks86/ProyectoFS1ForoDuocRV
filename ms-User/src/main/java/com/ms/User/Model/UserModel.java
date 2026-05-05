package com.ms.User.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Column(unique = true, nullable = false)
    private String username;

    @Column(length = 500)
    private String bio;

    private String avatarUrl;

    @Builder.Default
    private Integer reputationLevel = 0;

    @Builder.Default
    private Integer followersCount = 0;

    @Builder.Default
    private Integer followingCount = 0;
}
