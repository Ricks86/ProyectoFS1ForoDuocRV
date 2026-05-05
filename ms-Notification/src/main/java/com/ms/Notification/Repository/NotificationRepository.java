package com.ms.Notification.Repository;

import com.ms.Notification.Model.NotificationModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<NotificationModel, Long> {

    List<NotificationModel> findByRecipientUsernameOrderByCreatedAtDesc(String recipientUsername);

    Long countByRecipientUsernameAndIsReadFalse(String recipientUsername);

    List<NotificationModel> findByRecipientUsernameAndIsReadFalse(String recipientUsername);
}
