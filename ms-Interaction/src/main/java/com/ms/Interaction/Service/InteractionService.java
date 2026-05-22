package com.ms.Interaction.Service;

import com.ms.Interaction.Model.InteractionModel;
import com.ms.Interaction.Repository.InteractionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InteractionService {

    private final InteractionRepository interactionRepository;

    public InteractionModel createInteraction(String username, String entityType, Long entityId,
                                              String voteType) {

        String typeUpper = entityType.toUpperCase();

        if (!typeUpper.equals("POST") && !typeUpper.equals("COMMENT")) {
            throw new IllegalArgumentException("El tipo de entidad debe ser POST o COMMENT");
        }

        if (interactionRepository.findByUsernameAndEntityTypeAndEntityId(username, typeUpper, entityId).isPresent()) {
            throw new RuntimeException("Voto único: este usuario ya interactuó con esta publicación o comentario");
        }

        InteractionModel interaction = InteractionModel.builder()
                .username(username)
                .entityType(entityType)
                .entityId(entityId)
                .voteType(voteType)
                .build();

        return interactionRepository.save(interaction);
    }
}
