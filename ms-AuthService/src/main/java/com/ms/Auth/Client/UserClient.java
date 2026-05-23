package com.ms.Auth.Client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "ms-user", url = "http://localhost:8082/api/users")
public interface UserClient {

    @PostMapping("/init")
    void inicializarUsuario(@RequestBody Map<String, Object> initData);
}
