package com.ms.Interaction.Controller;

import com.ms.Interaction.Model.InteractionModel;
import com.ms.Interaction.Model.InteractionRequestDTO;
import com.ms.Interaction.Service.InteractionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/interactions")
@RequiredArgsConstructor
@Slf4j
public class InteractionController {

    private final InteractionService interactionService;

    @PostMapping("/vote")
    public ResponseEntity<InteractionModel> createInteraction(@Valid @RequestBody InteractionRequestDTO request) {
        log.info("Peticion de interaccion (voto) del usuario {} para {} con ID {}",
                request.getUsername(), request.getEntityType(), request.getEntityId());

        InteractionModel newInteraction = interactionService.createInteraction(
                request.getUsername(),
                request.getEntityType(),
                request.getEntityId(),
                request.getVoteType()
        );

        return new ResponseEntity<>(newInteraction, HttpStatus.CREATED);
    }
}
