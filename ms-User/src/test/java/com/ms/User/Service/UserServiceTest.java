package com.ms.User.Service;

import com.ms.User.DTOs.UserProfileDTO;
import com.ms.User.DTOs.UserUpdateDTO;
import com.ms.User.Model.UserModel;
import com.ms.User.Repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void testCreateInitialProfile() {
        UserModel user = new UserModel();
        user.setUsername("testuser");

        Mockito.when(userRepository.save(any(UserModel.class))).thenReturn(user);

        UserModel result = userService.createInitialProfile(1L, "testuser", "test@correo.com");

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void toGetProfile_Success() {
        UserModel user = new UserModel();
        user.setUsername("testuser");
        user.setBio("Bio test");

        Mockito.when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));

        UserProfileDTO result = userService.getProfile("testuser");

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void testUpdateProfile() {
        UserModel user = new UserModel();
        user.setUsername("testuser");
        user.setAuthId(1L);

        UserUpdateDTO updateDTO = new UserUpdateDTO();
        updateDTO.setBio("Nueva Bio");

        Mockito.when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        Mockito.when(userRepository.save(any(UserModel.class))).thenReturn(user);

        UserProfileDTO result = userService.updateProfile("testuser", updateDTO, 1L);

        assertNotNull(result);
        assertEquals("Nueva Bio", user.getBio());
    }
}
