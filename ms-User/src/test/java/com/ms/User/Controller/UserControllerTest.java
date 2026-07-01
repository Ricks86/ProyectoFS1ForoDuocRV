package com.ms.User.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ms.User.DTOs.UserDTO;
import com.ms.User.DTOs.UserInitDTO;
import com.ms.User.DTOs.UserProfileDTO;
import com.ms.User.DTOs.UserUpdateDTO;
import com.ms.User.Model.UserModel;
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

import static org.mockito.ArgumentMatchers.*;
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
    private AuditService auditoriaService;

    @Test
    void testInitProfile_Exito() throws Exception {
        UserInitDTO request = new UserInitDTO();
        request.setAuthId(100L);
        request.setUsername("testuser");
        request.setEmail("test@correo.com");

        UserModel mockUser = new UserModel();
        mockUser.setUsername("testuser");

        Mockito.when(userService.createInitialProfile(eq(100L), eq("testuser"), eq("test@correo.com")))
                .thenReturn(mockUser);

        mockMvc.perform(post("/api/users/init")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void testUpdateReputation_Exito() throws Exception {
        Mockito.doNothing().when(userService).addReputation(eq("testuser"), eq(10));

        mockMvc.perform(post("/api/users/testuser/reputation")
                        .param("points", "10"))
                .andExpect(status().isOk());

        Mockito.verify(userService, Mockito.times(1)).addReputation("testuser", 10);
    }

    @Test
    void testGetProfile_Exito() throws Exception {
        UserProfileDTO mockProfile = new UserProfileDTO();
        mockProfile.setUsername("testuser");

        Mockito.when(userService.getProfile("testuser")).thenReturn(mockProfile);

        mockMvc.perform(get("/api/users/perfil/testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void testObtenerPorId_Exito() throws Exception {
        UserDTO mockDto = new UserDTO(100L, "testuser", "Alias");

        Mockito.when(userService.obtenerUsuarioDtoPorId(100L)).thenReturn(mockDto);

        mockMvc.perform(get("/api/users/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void testObtenerPorUsername_Exito() throws Exception {
        UserDTO mockDto = new UserDTO(100L, "testuser", "Alias");

        Mockito.when(userService.obtenerUsuarioDtoPorUsername("testuser")).thenReturn(mockDto);

        mockMvc.perform(get("/api/users/username/testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void testUpdateProfile_Exito() throws Exception {
        UserUpdateDTO updateDTO = new UserUpdateDTO();
        updateDTO.setBio("Nueva Bio");

        UserProfileDTO updatedProfile = new UserProfileDTO();
        updatedProfile.setUsername("testuser");

        Mockito.when(jwtUtil.extractUserId(anyString())).thenReturn(100L);
        Mockito.when(userService.updateProfile(eq("testuser"), any(UserUpdateDTO.class), eq(100L)))
                .thenReturn(updatedProfile);

        mockMvc.perform(put("/api/users/actualizar/testuser")
                        .header("Authorization", "Bearer mockToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));

        Mockito.verify(auditoriaService, Mockito.times(1))
                .registrarLog(eq(100L), eq("UPDATE_PROFILE"), anyString());
    }
}
