package com.ms.Messasing.Model;

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
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El mensaje no puede estar vacío")
    @Size(max = 500, message = "El mensaje no puede exceder los 500 caracteres")
    @Column(name ="CONTENIDO",nullable = false, length = 500)
    private String contenido;

    @NotNull(message = "El emisor es obligatorio")
    @Column(name = "Id_EMISOR", nullable = false)
    private Long idEmisor;

    @NotNull(message = "El receptor es obligatorio")
    @Column(name = "Id_RECEPTOR", nullable = false)
    private Long idReceptor;

    @Column(name = "FECHA_ENVIO")
    private LocalDateTime fechaEnvio;

    @Column(name = "LEIDO")
    private boolean leido;

    @PrePersist
    public void alEnviar(){
        this.fechaEnvio = LocalDateTime.now();
        this.leido = false;
    }

}
