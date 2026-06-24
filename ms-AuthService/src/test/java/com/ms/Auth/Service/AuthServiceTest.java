package com.ms.Auth.Service;

import com.ms.Auth.Client.UserClient;
import com.ms.Auth.DTOs.LoginRequestDTO;
import com.ms.Auth.DTOs.RegisterRequestDTO;
import com.ms.Auth.DTOs.RegisterResponseDTO;
import com.ms.Auth.DTOs.TokenResponseDTO;
import com.ms.Auth.Model.*;
import com.ms.Auth.Repository.UserAuthRepository;
import com.ms.Auth.Security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @Mock
    private UserAuthRepository userAuthRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserClient userClient;

    @InjectMocks
    private AuthService authService;

    private RegisterRequestDTO registerRequest;
    private LoginRequestDTO loginRequest;
    private UserAuth mockUser;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequestDTO();
        registerRequest.setNombreUser("DuckyProtocol");
        registerRequest.setEmail("ducky@protocol.com");
        registerRequest.setPassword("password123");

        loginRequest = new LoginRequestDTO();
        loginRequest.setNombreUser("DuckyProtocol");
        loginRequest.setPassword("password123");

        mockUser = UserAuth.builder()
                .id(1L)
                .nombreUser("DuckyProtocol")
                .email("ducky@protocol.com")
                .password("hashedPassword")
                .build();
    }

    @Test
    void testRegister_OK() {
        Mockito.when(userAuthRepository.existsByNombreUser(anyString())).thenReturn(false);
        Mockito.when(userAuthRepository.existsByEmail(anyString())).thenReturn(false);
        Mockito.when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");

        Mockito.when(userAuthRepository.save(any(UserAuth.class))).thenReturn(mockUser);

        RegisterResponseDTO response = authService.register(registerRequest);

        assertNotNull(response);
        assertTrue(response.getMensaje().contains("registrado con éxito"));
        Mockito.verify(userClient, Mockito.times(1)).inicializarUsuario(anyMap());
    }

    @Test
    void testRegister_NombreUserUnAuthorized() {
        Mockito.when(userAuthRepository.existsByNombreUser("DuckyProtocol")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.register(registerRequest);
        });

        assertEquals("El nombre de usuario ya existe", exception.getMessage());
        Mockito.verify(userAuthRepository, Mockito.never()).save(any(UserAuth.class));
    }

    @Test
    void testRegister_EmailUnAuthorized() {
        Mockito.when(userAuthRepository.existsByNombreUser("DuckyProtocol")).thenReturn(false);
        Mockito.when(userAuthRepository.existsByEmail("ducky@protocol.com")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.register(registerRequest);
        });

        assertEquals("El email ya está en uso", exception.getMessage());
        Mockito.verify(userAuthRepository, Mockito.never()).save(any(UserAuth.class));
    }

    @Test
    void testRegister_500() {
        Mockito.when(userAuthRepository.existsByNombreUser(anyString())).thenReturn(false);
        Mockito.when(userAuthRepository.existsByEmail(anyString())).thenReturn(false);
        Mockito.when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");

        Mockito.doThrow(new RuntimeException("Error 500 ms-User caído"))
                .when(userClient).inicializarUsuario(anyMap());

        RegisterResponseDTO response = authService.register(registerRequest);

        assertNotNull(response);
        assertTrue(response.getMensaje().contains("registrado con éxito"));
        Mockito.verify(userAuthRepository, Mockito.times(1)).save(any(UserAuth.class));
    }

    @Test
    void testLogin_OK() {
        Mockito.when(userAuthRepository.findByNombreUser("DuckyProtocol")).thenReturn(Optional.of(mockUser));
        Mockito.when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(true);
        Mockito.when(jwtService.generateToken(mockUser)).thenReturn("eyTokenSimulado...");

        TokenResponseDTO response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("eyTokenSimulado...", response.getToken());
        assertEquals("DuckyProtocol", response.getUsername());
        assertEquals(1L, response.getUsuarioId());
    }

    @Test
    void testLogin_NotFound() {
        Mockito.when(userAuthRepository.findByNombreUser("UsuarioFantasma")).thenReturn(Optional.empty());

        loginRequest.setNombreUser("UsuarioFantasma");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("Credenciales Invalidas", exception.getMessage());
        Mockito.verify(jwtService, Mockito.never()).generateToken(any());
    }

    @Test
    void testLogin_UnAuthorized() {
        Mockito.when(userAuthRepository.findByNombreUser("DuckyProtocol")).thenReturn(Optional.of(mockUser));
        Mockito.when(passwordEncoder.matches("claveEquivocada", "hashedPassword")).thenReturn(false);

        loginRequest.setPassword("claveEquivocada");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("Credenciales invalidas", exception.getMessage());
        Mockito.verify(jwtService, Mockito.never()).generateToken(any());
    }
}

