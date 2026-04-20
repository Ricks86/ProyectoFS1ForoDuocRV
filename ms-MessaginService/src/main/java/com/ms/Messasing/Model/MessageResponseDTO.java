package com.ms.Messasing.Model;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponseDTO {
    private Long id;
    private String contenido;

    private Long emisorId;
    private String emisorNombre;

    private Long  receptorId;
    private String receptorNombre;

    private LocalDateTime enviadoEl;
    private boolean leido;
}
