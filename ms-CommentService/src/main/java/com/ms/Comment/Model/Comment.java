package com.ms.Comment.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El comentario no puede estar vacío")
    @Size(max =1000, message ="El comentario es demasiado largo (máx 1000 caracteres)")
    @Column(name = "CONTENIDO", nullable = false, length = 1000)
    private String content;

    @NotNull(message = "El Id de usuario es obligatorio")
    @Column(name = "ID_USUARIO", nullable = false)
    private Long userId;

    @NotNull(message = "El Id de post es obligatorio")
    @Column(name = "ID_POST", nullable = false)
    private Long postId;

    @Column(name = "FECHA_CREACION", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void alCrear() {
        this.createdAt = LocalDateTime.now();
    }


}
