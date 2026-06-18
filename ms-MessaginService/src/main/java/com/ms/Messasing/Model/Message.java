package com.ms.Messasing.Model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

@Schema(
        name = "Message",
        description = "Representa un mensaje privado enviado entre dos usuarios dentro del sistema."
)

public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(
            title = "Identificador único del mensaje",
            description = "Clave primaria autogenerada para identificar este mensaje.",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Long id;

    @NotBlank(message = "El mensaje no puede estar vacío")
    @Size(max = 500, message = "El mensaje no puede exceder los 500 caracteres")
    @Column(name = "CONTENIDO", nullable = false, length = 500)
    @Schema(
            title = "Contenido del mensaje",
            description = "Cuerpo del mensaje privado enviado. Soporta hasta 500 caracteres.",
            example = "Hola, ¿podrías ayudarme con la configuración de tu post sobre Linux?",
            maxLength = 500
    )
    private String contenido;

    @NotNull(message = "El emisor es obligatorio")
    @Column(name = "Id_EMISOR", nullable = false)
    @Schema(
            title = "ID del emisor",
            description = "Identificador del usuario que envía el mensaje (Obtenido vía FeignClient con ms-Users).",
            example = "10"
    )
    private Long idEmisor;

    @NotNull(message = "El receptor es obligatorio")
    @Column(name = "Id_RECEPTOR", nullable = false)
    @Schema(
            title = "ID del receptor",
            description = "Identificador del usuario que recibe el mensaje (Obtenido vía FeignClient con ms-Users).",
            example = "20"
    )
    private Long idReceptor;

    @Column(name = "FECHA_ENVIO")
    @Schema(
            title = "Fecha de envío",
            description = "Marca de tiempo automática que registra cuándo fue enviado el mensaje.",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private LocalDateTime fechaEnvio;

    @Column(name = "LEIDO")
    @Schema(
            title = "Estado de lectura",
            description = "Indica si el receptor ya ha visualizado el mensaje.",
            example = "false",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private boolean leido;

    @PrePersist
    public void alEnviar() {
        this.fechaEnvio = LocalDateTime.now();
        this.leido = false;
    }
}