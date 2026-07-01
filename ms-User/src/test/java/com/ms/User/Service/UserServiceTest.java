package com.ms.User.Service;

import com.ms.User.DTOs.UserDTO;
import com.ms.User.DTOs.UserProfileDTO;
import com.ms.User.DTOs.UserUpdateDTO;
import com.ms.User.Model.UserModel;
import com.ms.User.Repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private UserModel mockUser;
    private UserUpdateDTO mockUpdateDTO;

    @BeforeEach
    void setUp() {
        mockUser = new UserModel();
        mockUser.setId(1L);
        mockUser.setAuthId(100L);
        mockUser.setUsername("testuser");
        mockUser.setEmail("test@correo.com");
        mockUser.setBio("Bio original");
        mockUser.setAvatarUrl("http://avatar.com/old");
        mockUser.setReputationLevel(50);
        mockUser.setFollowersCount(10);
        mockUser.setFollowingCount(5);
        mockUser.setAlias("AliasTest");

        mockUpdateDTO = new UserUpdateDTO();
        mockUpdateDTO.setBio("Nueva Bio");
        mockUpdateDTO.setAvatarUrl("http://avatar.com/new");
    }

    @Test
    void testCreateInitialProfile_Exito() {
        Mockito.when(userRepository.existsByUsername("testuser")).thenReturn(false);
        Mockito.when(userRepository.save(any(UserModel.class))).thenReturn(mockUser);

        UserModel result = userService.createInitialProfile(100L, "testuser", "test@correo.com");

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        Mockito.verify(userRepository, Mockito.times(1)).save(any(UserModel.class));
    }

    @Test
    void testCreateInitialProfile_FallaPorExistencia() {
        Mockito.when(userRepository.existsByUsername("testuser")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.createInitialProfile(100L, "testuser", "test@correo.com");
        });

        assertEquals("El perfil de usuario ya existe", exception.getMessage());
        Mockito.verify(userRepository, Mockito.never()).save(any(UserModel.class));
    }

    @Test
    void testGetProfile_Exito() {
        Mockito.when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));

        UserProfileDTO result = userService.getProfile("testuser");

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals(50, result.getReputationLevel());
    }

    @Test
    void testGetProfile_NoEncontrado() {
        Mockito.when(userRepository.findByUsername("fantasma")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.getProfile("fantasma"));
    }

    @Test
    void testUpdateProfile_ExitoTotal() {
        Mockito.when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        Mockito.when(userRepository.save(any(UserModel.class))).thenReturn(mockUser);

        UserProfileDTO result = userService.updateProfile("testuser", mockUpdateDTO, 100L);

        assertNotNull(result);
        assertEquals("Nueva Bio", mockUser.getBio());
        assertEquals("http://avatar.com/new", mockUser.getAvatarUrl());
        Mockito.verify(userRepository, Mockito.times(1)).save(any(UserModel.class));
    }

    @Test
    void testUpdateProfile_ExitoParcial_DatosNulos() {
        UserUpdateDTO partialUpdate = new UserUpdateDTO();

        Mockito.when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        Mockito.when(userRepository.save(any(UserModel.class))).thenReturn(mockUser);

        UserProfileDTO result = userService.updateProfile("testuser", partialUpdate, 100L);

        assertNotNull(result);
        assertEquals("Bio original", mockUser.getBio());
        assertEquals("http://avatar.com/old", mockUser.getAvatarUrl());
    }

    @Test
    void testUpdateProfile_FallaPorAccesoDenegado() {
        Mockito.when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.updateProfile("testuser", mockUpdateDTO, 99L);
        });

        assertTrue(exception.getMessage().contains("Acceso denegado"));
        Mockito.verify(userRepository, Mockito.never()).save(any());
    }

    @Test
    void testUpdateProfile_NoEncontrado() {
        Mockito.when(userRepository.findByUsername("fantasma")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            userService.updateProfile("fantasma", mockUpdateDTO, 100L);
        });
    }

    @Test
    void testAddReputation_Exito() {
        Mockito.when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        Mockito.when(userRepository.save(any(UserModel.class))).thenReturn(mockUser);

        userService.addReputation("testuser", 15);

        assertEquals(65, mockUser.getReputationLevel());
        Mockito.verify(userRepository, Mockito.times(1)).save(any(UserModel.class));
    }

    @Test
    void testAddReputation_NoEncontrado() {
        Mockito.when(userRepository.findByUsername("fantasma")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.addReputation("fantasma", 10));
    }

    @Test
    void testObtenerUsuarioDtoPorId_Exito() {
        Mockito.when(userRepository.findByAuthId(100L)).thenReturn(Optional.of(mockUser));

        UserDTO result = userService.obtenerUsuarioDtoPorId(100L);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void testObtenerUsuarioDtoPorId_NoEncontrado() {
        Mockito.when(userRepository.findByAuthId(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.obtenerUsuarioDtoPorId(99L));
    }

    @Test
    void testObtenerUsuarioDtoPorUsername_Exito() {
        Mockito.when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));

        UserDTO result = userService.obtenerUsuarioDtoPorUsername("testuser");

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void testObtenerUsuarioDtoPorUsername_NoEncontrado() {
        Mockito.when(userRepository.findByUsername("fantasma")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.obtenerUsuarioDtoPorUsername("fantasma"));
    }
}