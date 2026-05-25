package com.ms.Interaction.Controller;

import com.ms.Interaction.Model.InteractionModel;
import com.ms.Interaction.Model.InteractionRequestDTO;
import com.ms.Interaction.Security.JwtUtil;
import com.ms.Interaction.Service.InteractionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/interactions")
@Slf4j
@RequiredArgsConstructor
public class InteractionController {

    private final InteractionService interactionService;
    private final JwtUtil jwtUtil;

    @Autowired
    public InteractionController(InteractionService interactionService, JwtUtil jwtUtil) {
        this.interactionService = interactionService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/vote")
    public ResponseEntity<InteractionModel> createInteraction(
        @RequestHeader("Authorization") String token,
        @Valid @RequestBody InteractionRequestDTO request) {

        String jwt = token.substring(7);

        Long userId = jwtUtil.extractUserId(jwt);
        String secureUsername = jwtUtil.extractUsername(jwt);

        InteractionModel newInteraction = interactionService.createInteraction(
                userId,
                secureUsername,
                request.getEntityType(),
                request.getEntityId(),
                request.getVoteType()
        );

        return new ResponseEntity<>(newInteraction, HttpStatus.CREATED);
    }
}
