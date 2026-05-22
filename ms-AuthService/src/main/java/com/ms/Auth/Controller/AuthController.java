package com.ms.Auth.Controller;

import com.ms.Auth.Model.LoginRequestDTO;
import com.ms.Auth.Model.RegisterRequestDTO;
import com.ms.Auth.Model.TokenResponseDTO;
import com.ms.Auth.Model.UserAuth;
import com.ms.Auth.Service.AuditService;
import com.ms.Auth.Service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.Token;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final AuditService auditoriaService;

    @PostMapping("/login")
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
    public ResponseEntity<TokenResponseDTO> register(@RequestBody RegisterRequestDTO request) {
        TokenResponseDTO response = authService.register(request);

        auditoriaService.registrarLog(
                response.getUsuarioId(),
                "REGISTER",
                "Nuevo registro de cuenta para: " + request.getNombreUser()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }



}
