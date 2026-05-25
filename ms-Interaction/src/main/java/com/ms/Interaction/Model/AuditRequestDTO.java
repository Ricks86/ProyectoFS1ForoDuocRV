package com.ms.Interaction.Model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuditRequestDTO {

    @NotNull(message = "El ID del usuario es obligatorio")
    private Long usuarioId;

    @NotBlank(message = "La acción es obligatoria")
    @Size(max = 50, message = "La acción no debe superar los 50 caracteres")
    private String accion;

    @NotBlank(message = "El recurso es obligatorio")
    @Size(max = 50, message = "El recurso no debe superar los 50 caracteres")
    private String recurso;

    @NotBlank(message = "Los detalles son obligatorios")
    @Size(max = 500, message = "Los detalles no deben superar los 500 caracteres")
    private String detalles;
}
