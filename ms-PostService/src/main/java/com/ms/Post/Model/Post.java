package com.ms.Post.Model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "posts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

@Schema(
        name = "Post",
        description = "Representa una publicación o hilo de discusión creado por un usuario"
)

public class Post {

    @Schema(title = "Identificador único",
            example = "101",
            accessMode = Schema.AccessMode.READ_ONLY)

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(title = "Título del post",
            description = "Encabezado principal de la publicación",
            example = "¿Cómo configurar Wayland en Nobara?")

    @NotBlank(message = "El titulo es obligatorio")
    @Size(min = 5, max = 64, message = "El titulo debe tener entre 5 y 64 caracteres")
    @Column (name = "TITULO", nullable = false)
    private String titulo;

    @Schema(title = "Contenido",
            description = "Cuerpo detallado de la publicación",
            example = "He estado experimentando problemas con los drivers al instalar archivos RPM...")

    @NotBlank(message = "El contenido no puede estar vacio")
    @Size(min = 10, message = "El contenido debe tener al menos 10 caracteres")
    @Column(name = "CONTENIDO", nullable = false,length = 2000)
    private String contenido;

    @Schema(title = "ID del Autor",
            description = "Identificador (Soft Link) del UserModel que creó el post",
            example = "1")

    @NotNull(message = "El id del usuario es obligatorio")
    @Column(name = "ID_USUARIO", nullable = false)
    private Long idUsuario;

    @Schema(title = "ID de la Comunidad",
            description = "Identificador de la comunidad donde se publicó. Es nulo si el post es público.",
            example = "5",
            nullable = true)

    @Column(name = "ID_COMUNIDAD")
    private Long idComunidad;

    @Schema(title = "Fecha de creación",
            accessMode = Schema.AccessMode.READ_ONLY)

    @Column(name = "FECHA_CREACION", nullable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    public void alCrear() {
        this.fechaCreacion = LocalDateTime.now();
    }
}
