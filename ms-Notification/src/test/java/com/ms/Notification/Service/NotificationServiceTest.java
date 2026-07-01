package com.ms.Notification.Service;

import com.ms.Notification.Client.UserClient;
import com.ms.Notification.DTOs.NotificationResponseDTO;
import com.ms.Notification.Model.NotificationModel;
import com.ms.Notification.Repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
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

    @Test
    void testGetUserNotifications() {
        Mockito.when(notificationRepository.findByRecipientIdOrderByCreatedAtDesc(anyLong()))
                .thenReturn(Collections.emptyList());

        List<NotificationResponseDTO> result = notificationService.getUserNotifications(1L, "token");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetUnreadCount() {
        Mockito.when(notificationRepository.countByRecipientIdAndIsReadFalse(anyLong())).thenReturn(5L);

        Long count = notificationService.getUnreadCount(1L);

        assertEquals(5L, count);
    }

    @Test
    void testMarkAsRead() {
        NotificationModel notif = new NotificationModel();
        notif.setRecipientId(1L);
        notif.setIsRead(false);

        Mockito.when(notificationRepository.findById(anyLong())).thenReturn(Optional.of(notif));
        Mockito.when(notificationRepository.save(any(NotificationModel.class))).thenReturn(notif);

        notificationService.markAsRead(10L, 1L);

        assertTrue(notif.getIsRead());
    }

}