package com.ms.Interaction.Client;

import com.ms.Interaction.DTOs.AuditRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "ms-Audit", url = "http://localhost:8090/api/audit")
public interface AuditClient {
    @PostMapping
    void registrarAccion(@RequestBody AuditRequestDTO auditoria);
}
