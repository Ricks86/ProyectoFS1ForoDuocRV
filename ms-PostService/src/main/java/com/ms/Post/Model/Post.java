package com.ms.Post.Model;

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
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El titulo es obligatorio")
    @Size(min = 5, max = 64, message = "El titulo debe tener entre 5 y 64 caracteres")
    @Column (name = "TITULO", nullable = false)
    private String titulo;

    @NotBlank(message = "El contenido no puede estar vacio")
    @Size(min = 10, message = "El contenido debe tener al menos 10 caracteres")
    @Column(name = "CONTENIDO", nullable = false,length = 2000)
    private String contenido;

    //relaciones

    @NotNull(message = "El id del usuario es obligatorio")
    @Column(name = "ID_USUARIO", nullable = false)
    private Long idUsuario;

    @Column(name = "ID_COMUNIDAD")
    private Long idComunidad;

    @Column(name = "FECHA_CREACION", nullable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    public void alCrear() {
        this.fechaCreacion = LocalDateTime.now();
    }
}
