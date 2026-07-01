package com.ms.Interaction.Controller;

import com.ms.Interaction.DTOs.InteractionRequestDTO;
import com.ms.Interaction.DTOs.InteractionResponseDTO;
import com.ms.Interaction.Security.JwtUtil;
import com.ms.Interaction.Service.AuditService;
import com.ms.Interaction.Service.InteractionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Interacciones", description = "Gestión de votos en publicaciones y comentarios")
public class InteractionController {

    private final InteractionService interactionService;
    private final JwtUtil jwtUtil;
    private final AuditService auditoriaService;

    @PostMapping("/vote")
    @Operation(summary = "Alternar voto", description = "Agrega, actualiza o elimina un voto en una entidad.")
    @ApiResponse(responseCode = "200", description = "Voto procesado correctamente")
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
    @Operation(summary = "Obtener votos de una entidad", description = "Retorna la lista de las interacciones que ha recibido un post o comentario.")
    @ApiResponse(responseCode = "200", description = "Interacciones obtenidas exitosamente")
    public ResponseEntity<List<InteractionResponseDTO>> getVotesByEntity(
            @PathVariable String entityType,
            @PathVariable Long entityId,
            @RequestHeader("Authorization") String token) {

        return ResponseEntity.ok(interactionService.getVotesForEntity(entityType, entityId, token));
    }
}
