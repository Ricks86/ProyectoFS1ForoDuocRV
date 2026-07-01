package com.ms.Notification.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ms.Notification.DTOs.NotificationCreateDTO;
import com.ms.Notification.DTOs.NotificationResponseDTO;
import com.ms.Notification.Security.JwtUtil;
import com.ms.Notification.Service.AuditService;
import com.ms.Notification.Service.NotificationService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
public class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private AuditService auditoriaService;

    @Test
    void testCreateNotification() throws Exception {
        NotificationCreateDTO request = NotificationCreateDTO.builder()
                .recipientId(2L)
                .Type("INFO")
                .message("Mensaje de prueba")
                .build();

        NotificationResponseDTO response = new NotificationResponseDTO();

        Mockito.when(notificationService.createNotification(any(NotificationCreateDTO.class), anyString()))
                .thenReturn(response);

        mockMvc.perform(post("/api/notifications")
                        .header("X-Service-Origin", "SYSTEM")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    void testMyNotification() throws Exception {
        Mockito.when(jwtUtil.extractUserId(anyString())).thenReturn(1L);
        Mockito.when(notificationService.getUserNotifications(any(Long.class), anyString()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/notifications/mis-notificaciones")
                .header("Authorization", "Bearer token-falso"))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    void testGetMyUnreadCount() throws Exception {
        Mockito.when(jwtUtil.extractUserId(anyString())).thenReturn(1L);
        Mockito.when(notificationService.getUnreadCount(any(Long.class))).thenReturn(5L);

        mockMvc.perform(get("/api/notifications/no-leidas")
                .header("Authorization", "Bearer token-falso"))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    void MarkAsRead() throws Exception {
        Mockito.when(jwtUtil.extractUserId(anyString())).thenReturn(1L);

        mockMvc.perform(put("/api/notifications/1/read")
                        .header("Authorization", "Bearer token-falso"))
                .andExpect(status().is2xxSuccessful());
    }
}