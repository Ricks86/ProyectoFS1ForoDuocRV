package com.ms.Auth.Controller;

import com.ms.Auth.Model.*;
import com.ms.Auth.Service.AuditService;
import com.ms.Auth.Service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j

@Tag(   name = "Autenticación y Registro",
        description = "Endpoints públicos del proveedor de identidad (IdP) para el inicio de sesión y la creación de cuentas de usuario")
public class AuthController {

    private final AuthService authService;
    private final AuditService auditoriaService;

    @PostMapping("/login")
    @Operation(
            summary = "Iniciar sesión (Login)",
            description = "Valida las credenciales del usuario (nombre de usuario y contraseña). Si son válidas, emite un token JWT firmado y gatilla un log asíncrono en ms-Audit."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Autenticación exitosa. Se devuelve el Token JWT y los datos base del usuario."),
            @ApiResponse(responseCode = "403", description = "Credenciales inválidas (Nombre de usuario o contraseña incorrectos)."),
            @ApiResponse(responseCode = "400", description = "Estructura JSON mal formada.")
    })
    public ResponseEntity<TokenResponseDTO> login(@RequestBody LoginRequestDTO request) {
        TokenResponseDTO response = authService.login(request);

        auditoriaService.registrarLog(
                response.getUsuarioId(),
                "LOGIN",
                "Inicio de sesión exitoso para el usuario: " + request.getNombreUser()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    @Operation(
            summary = "Registrar una nueva cuenta de usuario",
            description = "Da de alta las credenciales de acceso en la base de datos de Auth, encripta la contraseña usando algoritmos hash seguros, sincroniza el perfil con ms-User y genera un log de auditoría."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cuenta creada y perfil inicializado con éxito."),
            @ApiResponse(responseCode = "403", description = "El nombre de usuario o el correo electrónico ya se encuentran registrados en el sistema."),
            @ApiResponse(responseCode = "400", description = "Estructura JSON mal formada.")
    })
    public ResponseEntity<RegisterResponseDTO> register(@RequestBody RegisterRequestDTO request) {
        RegisterResponseDTO response = authService.register(request);

        auditoriaService.registrarLog(
                response.getUsuarioId(),
                "REGISTER",
                "Nuevo registro de cuenta para: " + request.getNombreUser()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }



}
