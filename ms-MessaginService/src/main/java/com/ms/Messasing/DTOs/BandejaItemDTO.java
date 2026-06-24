package com.ms.Messasing.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BandejaItemDTO {
    private UserDTO usuario;
    private Long mensajesSinLeer;
    private LocalDateTime ultimoMensaje;
}
