package com.ms.Auth.Controller;

import com.ms.Auth.DTOs.LoginRequestDTO;
import com.ms.Auth.DTOs.RegisterRequestDTO;
import com.ms.Auth.DTOs.RegisterResponseDTO;
import com.ms.Auth.DTOs.TokenResponseDTO;
import com.ms.Auth.Service.AuditService;
import com.ms.Auth.Service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {
    @Mock
    private AuthService authService;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private AuthController authController;

    private LoginRequestDTO loginRequest;
    private RegisterRequestDTO registerRequest;

    private TokenResponseDTO tokenResponse;
    private RegisterResponseDTO registerResponse;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequestDTO();
        loginRequest.setNombreUser("DuckyProtocol");
        loginRequest.setPassword("password123");
        tokenResponse = new TokenResponseDTO("fake-jwt-token-123", "DuckyProtocol",50L);

        registerRequest = new RegisterRequestDTO();
        registerRequest.setNombreUser("DuckyProtocol");
        registerRequest.setEmail("Ducky@email.com");
        registerRequest.setPassword("password123");
        registerResponse = new RegisterResponseDTO(50L,"Creación de usuario exitosos");
    }

    @Test
    void loginTest() {
        when(authService.login(loginRequest)).thenReturn(tokenResponse);

        ResponseEntity<TokenResponseDTO> response = authController.login(loginRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("fake-jwt-token-123", response.getBody().getToken());

        verify(auditService, times(1)).registrarLog(
                50L,
                "LOGIN",
                "Inicio de sesión exitoso para el usuario: DuckyProtocol"
        );
    }

    @Test
    void RegisterTest() {
        when(authService.register(registerRequest)).thenReturn(registerResponse);

        ResponseEntity<RegisterResponseDTO> response = authController.register(registerRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(50L, response.getBody().getUsuarioId());

        verify(auditService, times(1)).registrarLog(
                50L,
                "REGISTER",
                "Nuevo registro de cuenta para: " + registerRequest.getNombreUser()
        );
    }
}
