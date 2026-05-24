package com.ms.User.Client;

import com.ms.User.Model.AuditRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "ms-Users", url = "http://localhost:8090/api/audit")
public interface AuditClient {
    @PostMapping
    void registrarAccion(@RequestBody AuditRequestDTO auditoria);
}
