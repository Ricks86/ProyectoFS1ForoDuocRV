package com.ms.Messasing.Client;

import com.ms.Messasing.DTOs.NotificationCreateDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "ms-Notification", url = "http://localhost:8086/api/notifications")
public interface NotificationClient {
    @PostMapping
    void enviarNotificacion(@RequestBody NotificationCreateDTO request, @RequestHeader("X-Service-Origin") String serviceOrigin);
}

