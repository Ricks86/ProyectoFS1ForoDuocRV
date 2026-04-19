package com.ms.Post.Model;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostResponseDTO {
    private Long id;
    private String titulo;
    private String contenido;
    private Long idUsuario;
    private Long idComunidad;
    private LocalDateTime fechaCreacion;
}
