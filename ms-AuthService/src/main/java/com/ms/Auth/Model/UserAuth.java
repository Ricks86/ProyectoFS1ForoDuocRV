package com.ms.Auth.Model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_auth")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

@Schema(
        name = "UserAuth",
        description = "Entidad central que custodia las credenciales criptográficas y los datos de acceso para la autenticación del usuario."
)

public class UserAuth {

    @Schema(
            title = "Identificador único de autenticación",
            description = "Clave primaria autogenerada en la base de datos de Auth. Actúa como el Soft Link del resto del ecosistema.",
            example = "1",
            accessMode = Schema.AccessMode.READ_ONLY
    )

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(
            title = "Nombre de cuenta del usuario",
            description = "Identificador único alfanumérico utilizado para el proceso de Login.",
            example = "DuckyProtocol",
            minLength = 4,
            maxLength = 32
    )

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 4, max = 32, message = "El usuario debe tener entre 4 y 32 caracteres")
    @Column(unique = true, nullable = false)
    private String nombreUser;

    @Schema(
            title = "Correo electrónico único",
            description = "Dirección de correo electrónico validada del usuario",
            example = "ducky.protocol@duocuc.cl"
    )

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El correo electrónico no es válido")
    @Column(unique = true, nullable = false)
    private String email;

    @Schema(
            title = "Contraseña de acceso",
            description = "Clave secreta cifrada mediante un algoritmo hash seguro (BCrypt). En la base de datos nunca se guarda en texto plano.",
            accessMode = Schema.AccessMode.WRITE_ONLY
    )

    @NotBlank(message = "La contraseña es obligatorio")
    @Column(nullable = false)
    private String password;

    @Schema(
            title = "Fecha y hora de registro",
            accessMode = Schema.AccessMode.READ_ONLY
    )

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

}
