package com.ms.Interaction.Service;

import com.ms.Interaction.Client.AuditClient;
import com.ms.Interaction.Model.AuditRequestDTO;
import com.ms.Interaction.Model.InteractionModel;
import com.ms.Interaction.Repository.InteractionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InteractionService {

    private final InteractionRepository interactionRepository;
    private final AuditClient auditClient;

    @Autowired
    public InteractionService(InteractionRepository interactionRepository, AuditClient auditClient) {
        this.interactionRepository = interactionRepository;
        this.auditClient = auditClient;
    }

    public InteractionModel createInteraction(Long userId, String username, String entityType, Long entityId,
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

        InteractionModel savedInteraction = interactionRepository.save(interaction);

        enviarAuditoria(userId, "CREATE_INTERACTION", "El usuario" + username + "votó" +
                voteType + "en" + typeUpper + "ID:" + entityId);

        return savedInteraction;
    }

    private void enviarAuditoria(Long usuarioId, String accion, String detalles) {
        try {
            AuditRequestDTO auditoria = new AuditRequestDTO();
            auditoria.setUsuarioId(usuarioId);
            auditoria.setAccion(accion);
            auditoria.setRecurso("ms-Interaction");
            auditoria.setDetalles(detalles);

            auditClient.registrarAccion(auditoria);
        } catch (Exception e) {
            System.err.println("Aviso: No se pudo conectar con ms-Audit - " + e.getMessage());
        }
    }
}
