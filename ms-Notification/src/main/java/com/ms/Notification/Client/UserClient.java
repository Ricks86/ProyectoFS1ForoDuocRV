package com.ms.Notification.Client;

import com.ms.Notification.Model.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "ms-user", url = "http://localhost:8082/api/users")
public interface UserClient {

    @GetMapping("/{id}")
    UserDTO obtenerUsuarioPorId(@PathVariable("id") Long id, @RequestHeader("Authorization") String token);

    @GetMapping("/username/{username}")
    UserDTO obtenerUsuarioPorUsername(@PathVariable("username") String username, @RequestHeader("Authorization") String token);
}