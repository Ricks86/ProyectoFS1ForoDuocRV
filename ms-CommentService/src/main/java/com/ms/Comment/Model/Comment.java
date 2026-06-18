package com.ms.Comment.Model;

import io.swagger.v3.oas.annotations.media.Schema;
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

@Schema(
        name = "Comment",
        description = "Representa una respuesta o comentario realizado por un usuario dentro de una publicación específica."
)
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(
            title = "Identificador único del comentario",
            description = "Clave primaria autogenerada.",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Long id;

    @NotBlank(message = "El comentario no puede estar vacío")
    @Size(max = 1000, message = "El comentario es demasiado largo (máx 1000 caracteres)")
    @Column(name = "CONTENIDO", nullable = false, length = 1000)
    @Schema(
            title = "Contenido del comentario",
            description = "Texto escrito por el usuario. Soporta hasta 1000 caracteres.",
            maxLength = 1000
    )
    private String content;

    @NotNull(message = "El Id de usuario es obligatorio")
    @Column(name = "ID_USUARIO", nullable = false)
    @Schema(
            title = "ID del autor",
            description = "Identificador lógico del usuario autor del comentario (Obtenido vía FeignClient con ms-Users)."
    )
    private Long userId;

    @NotNull(message = "El Id de post es obligatorio")
    @Column(name = "ID_POST", nullable = false)
    @Schema(
            title = "ID de la publicación",
            description = "Identificador lógico del post al cual pertenece este comentario (Obtenido vía FeignClient con ms-Post=."
    )
    private Long postId;

    @Column(name = "FECHA_CREACION", nullable = false)
    @Schema(
            title = "Fecha de creación",
            description = "Marca de tiempo automática que registra cuándo fue publicado el comentario.",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private LocalDateTime createdAt;

    @PrePersist
    protected void alCrear() {
        this.createdAt = LocalDateTime.now();
    }
}
