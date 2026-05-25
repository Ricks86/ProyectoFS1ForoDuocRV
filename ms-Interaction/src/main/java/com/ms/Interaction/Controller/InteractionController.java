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

    @PostMapping("/like")
    public ResponseEntity<InteractionResponseDTO> toggleLike(
            @Valid @RequestBody InteractionRequestDTO request,
            @RequestHeader("Authorization") String token) {

        Long userId = jwtUtil.extractUserId(token);
        log.info("Petición de LIKE procesada para AuthID: {}", userId);

        InteractionResponseDTO response = interactionService.toggleLike(request, userId, token);

        auditoriaService.registrarLog(
                userId,
                response.getStatus(),
                "El usuario interactuó con el Post ID [" + request.getPostId() + "]"
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/post/{postId}/likes")
    public ResponseEntity<List<InteractionResponseDTO>> getLikesByPost(
            @PathVariable Long postId,
            @RequestHeader("Authorization") String token) {

        return ResponseEntity.ok(interactionService.getLikesForPost(postId, token));
    }
}
