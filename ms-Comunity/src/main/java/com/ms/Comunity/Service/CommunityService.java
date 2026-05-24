package com.ms.Comunity.Service;

import com.ms.Comunity.Client.AuditClient;
import com.ms.Comunity.Model.AuditRequestDTO;
import com.ms.Comunity.Model.CommunityModel;
import com.ms.Comunity.Repository.CommunityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CommunityService {

    private final CommunityRepository communityRepository;
    private final AuditClient auditClient;

    @Autowired
    public CommunityService(CommunityRepository communityRepository, AuditClient auditClient) {
        this.communityRepository = communityRepository;
        this.auditClient = auditClient;
    }

    public CommunityModel createCommunity(String name, String description, String creatorUsername, Long creatorId) {

        if (communityRepository.findByName(name).isPresent()) {
            throw new RuntimeException("Ya existe una comunidad con ese nombre");
        }

        String generatedAccessCode = UUID.randomUUID().toString().substring(0, 8);

        CommunityModel community = CommunityModel.builder()
                .name(name)
                .description(description)
                .creatorUsername(creatorUsername)
                .accessCode(generatedAccessCode)
                .memberCount(1)
                .build();

        if (community.getMemberIds() != null) {
            community.getMemberIds().add(creatorId);
        }

        CommunityModel savedCommunity = communityRepository.save(community);

        enviarAuditoria(creatorId, "CREATE_COMMUNITY", "Comunidad '" + name + "' creada exitosamente por: " + creatorUsername);

        return savedCommunity;
    }

    public CommunityModel joinCommunity(Long userId, String accessCode) {

        CommunityModel community = communityRepository.findByAccessCode(accessCode)
                .orElseThrow(() -> new RuntimeException("Código de acceso inválido o comunidad no encontrada"));

        if (!community.getMemberIds().contains(userId)) {
            community.getMemberIds().add(userId);
            community.setMemberCount(community.getMemberCount() + 1);

            CommunityModel updatedCommunity = communityRepository.save(community);

            enviarAuditoria(userId, "JOIN_COMMUNITY", "Usuario ID [" + userId + "] se unió a la comunidad: " + community.getName());

            return updatedCommunity;
        } else {
            throw new RuntimeException("El usuario ya pertenece a esta comunidad");
        }
    }

    private void enviarAuditoria(Long usuarioId, String accion, String detalles) {
        try {
            AuditRequestDTO auditoria = new AuditRequestDTO();
            auditoria.setUsuarioId(usuarioId);
            auditoria.setAccion(accion);
            auditoria.setRecurso("ms-Comunity");
            auditoria.setDetalles(detalles);

            auditClient.registrarAccion(auditoria);
        } catch (Exception e) {
            System.err.println("Aviso: No se pudo conectar con ms-Audit - " + e.getMessage());
        }
    }
}
