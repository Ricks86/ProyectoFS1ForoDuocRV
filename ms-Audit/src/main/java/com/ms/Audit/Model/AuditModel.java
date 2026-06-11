package com.ms.Audit.Model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name= "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

@Schema(
        name= "AuditModel",
        description = "Representa los atributos bases que debe de tener un registro de log"
)
public class AuditModel {

    @Schema(
            title = "Identificador único para cada registro",
            example = "1",
            accessMode = Schema.AccessMode.READ_ONLY
    )

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(
            title = "identificador de usuario",
            description = "El id del usuario involucrado en una acción",
            example = "1"
    )

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Schema(
            title = "Acción realizada",
            description = "Cada microservicio tendrá un AuditService donde se definirán las acciones del propio microservicio",
            example = "Crear usuario"
    )

    @Column(nullable = false, length = 50)
    private String accion;

    @Schema(
            title = "Microservicio involucrado",
            description = "EL nombre del microservicio donde se disparo un petición",
            example = "Ms-Post"
    )

    @Column(nullable = false, length = 50)
    private String recurso;

    @Schema(
            title = "datos extras de cada acción",
            description = "usualmente especifican que usuario hizo x cosa o detallan mejor la acción",
            example = "Inicio de sesión exitoso para el usuario: "
    )

    @Column(nullable = false, length = 500)
    private String detalles;

    @Schema(
            title = "Fecha y hora",
            description = "fecha y hora exacta de cuando se creo el registro",
            accessMode = Schema.AccessMode.READ_ONLY
    )

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime fecha = LocalDateTime.now();
}
