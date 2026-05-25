package com.ms.Interaction.Controller;

import com.ms.Interaction.Model.InteractionRequestDTO;
import com.ms.Interaction.Model.InteractionResponseDTO;
import com.ms.Interaction.Security.JwtUtil;
import com.ms.Interaction.Service.AuditService;
import com.ms.Interaction.Service.InteractionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/interactions")
@Slf4j
@RequiredArgsConstructor
public class InteractionController {

    private final InteractionService interactionService;
    private final JwtUtil jwtUtil;
    private final AuditService auditoriaService;

    @PostMapping("/vote")
    public ResponseEntity<InteractionResponseDTO> toggleVote(
            @Valid @RequestBody InteractionRequestDTO request,
            @RequestHeader("Authorization") String token) {

        Long userId = jwtUtil.extractUserId(token);
        log.info("Petición de VOTO procesada para AuthID: {}", userId);

        InteractionResponseDTO response = interactionService.toggleVote(request, userId, token);

        auditoriaService.registrarLog(
                userId,
                response.getStatus(),
                "El usuario interactuó con la entidad [" + request.getEntityType() + "] ID [" + request.getEntityId() + "]"
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    public ResponseEntity<List<InteractionResponseDTO>> getVotesByEntity(
            @PathVariable String entityType,
            @PathVariable Long entityId,
            @RequestHeader("Authorization") String token) {

        return ResponseEntity.ok(interactionService.getVotesForEntity(entityType, entityId, token));
    }
}
