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
    private int id;

    @NotBlank(message = "El mensaje no puede estar vacío")
    @Size(max = 500, message = "El mensaje no puede exceder los 500 caracteres")
    @Column(name ="CONTENIDO",nullable = false, length = 500)
    private String contenido;

    @NotNull(message = "El emisor es obligatorio")
    @Column(name = "Id_EMISOR", nullable = false)
    private int idEmisor;

    @NotNull(message = "El receptor es obligatorio")
    @Column(name = "Id_RECEPTOR", nullable = false)
    private int idReceptor;

    @Column(name = "FECHA_ENVIO")
    private LocalDateTime fechaEnvio;

    @Column(name = "LEIDO")
    private boolean leido;

    @PrePersist
    public void alEnviar(){
        this.fechaEnvio = LocalDateTime.now();
        this.leido = true;
    }

}
