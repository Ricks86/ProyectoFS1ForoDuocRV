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
    private LocalDateTime fechaEnvio;
    private boolean leido;

    private UserDTO emisor;
    private UserDTO receptor;
}
