package com.ms.Notification.Service;

import com.ms.Notification.Client.UserClient;
import com.ms.Notification.DTOs.NotificationCreateDTO;
import com.ms.Notification.DTOs.NotificationResponseDTO;
import com.ms.Notification.DTOs.UserDTO;
import com.ms.Notification.Model.NotificationModel;
import com.ms.Notification.Repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private UserClient userClient;
    @InjectMocks
    private NotificationService notificationService;

    private NotificationModel mockNotif;
    private UserDTO mockUser;

    @BeforeEach
    void setUp() {
        mockNotif = NotificationModel.builder()
                .id(1L)
                .recipientId(10L)
                .senderId(20L)
                .type("LIKE")
                .serviceOrigin("SYSTEM")
                .message("Te dieron like")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
        mockUser = new UserDTO(20L, "sender", "Alias");
    }

    @Test
    void testCreateNotification() {
        NotificationCreateDTO dto = new NotificationCreateDTO(10L, 20L, "LIKE", "msg", 5L);
        Mockito.when(notificationRepository.save(any(NotificationModel.class))).thenReturn(mockNotif);

        NotificationResponseDTO res = notificationService.createNotification(dto, "SYSTEM");

        assertNotNull(res);
        assertEquals("LIKE", res.getType());
    }

    @Test
    void testGetUserNotifications() {
        Mockito.when(notificationRepository.findByRecipientIdOrderByCreatedAtDesc(10L)).thenReturn(List.of(mockNotif));
        Mockito.when(userClient.obtenerUsuarioPorId(eq(20L), anyString())).thenReturn(mockUser);

        List<NotificationResponseDTO> res = notificationService.getUserNotifications(10L, "token");

        assertEquals(1, res.size());
        assertEquals("sender", res.get(0).getSender().getUsername());
    }

    @Test
    void testGetUnreadCount() {
        Mockito.when(notificationRepository.countByRecipientIdAndIsReadFalse(10L)).thenReturn(5L);
        Long count = notificationService.getUnreadCount(10L);
        assertEquals(5L, count);
    }

    @Test
    void testMarkAsRead_Success() {
        Mockito.when(notificationRepository.findById(1L)).thenReturn(Optional.of(mockNotif));

        notificationService.markAsRead(1L, 10L);

        assertTrue(mockNotif.getIsRead());
        Mockito.verify(notificationRepository, Mockito.times(1)).save(mockNotif);
    }

    @Test
    void testMarkAsRead_NotFound() {
        Mockito.when(notificationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            notificationService.markAsRead(99L, 10L);
        });
    }

    @Test
    void testMarkAsRead_Unauthorized() {
        Mockito.when(notificationRepository.findById(1L)).thenReturn(Optional.of(mockNotif));

        assertThrows(RuntimeException.class, () -> {
            notificationService.markAsRead(1L, 999L);
        });
    }
}
