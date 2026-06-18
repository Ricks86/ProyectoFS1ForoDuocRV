package com.ms.User.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ms.User.Model.UserInitDTO;
import com.ms.User.Model.UserModel;
import com.ms.User.Model.UserProfileDTO;
import com.ms.User.Model.UserUpdateDTO;
import com.ms.User.Security.JwtUtil;
import com.ms.User.Service.AuditService;
import com.ms.User.Service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private AuditService auditService;

    @Test
    void testInitProfile_Success() throws Exception {
        UserInitDTO request = new UserInitDTO();

        request.setAuthId(1L);
        request.setUsername("testuser");
        request.setEmail("test@correo.cl");

        UserModel mockUser = UserModel.builder()
                .authId(1L)
                .username("testuser")
                .email("test@correo.cl")
                .build();

        Mockito.when(userService.createInitialProfile(anyLong(), anyString(), anyString()))
                .thenReturn(mockUser);

        mockMvc.perform(post("/api/users/init")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@correo.cl"));
    }

    @Test
    void testGetProfile_Success() throws Exception {
        UserProfileDTO mockProfile = UserProfileDTO.builder()
                .username("testuser")
                .bio("Esta es mi bio")
                .reputationLevel(10)
                .build();

        Mockito.when(userService.getProfile("testuser")).thenReturn(mockProfile);

        mockMvc.perform(get("/api/users/perfil/testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.bio").value("Esta es mi bio"))
                .andExpect(jsonPath("$.reputationLevel").value(10));
    }


    @Test
    void testUpdateProfile_Success() throws Exception {
        UserUpdateDTO updateRequest = new UserUpdateDTO();
        updateRequest.setBio("Nueva biografía actualizada");

        UserProfileDTO updatedProfile = UserProfileDTO.builder()
                .username("testuser")
                .bio("Nueva biografía actualizada")
                .build();

        Mockito.when(jwtUtil.extractUserId(anyString())).thenReturn(1L);

        Mockito.when(userService.updateProfile(anyString(), any(UserUpdateDTO.class), anyLong()))
                .thenReturn(updatedProfile);


        mockMvc.perform(put("/api/users/actualizar/testuser")
                        .header("Authorization", "Bearer token-falso")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bio").value("Nueva biografía actualizada"));
    }
}
