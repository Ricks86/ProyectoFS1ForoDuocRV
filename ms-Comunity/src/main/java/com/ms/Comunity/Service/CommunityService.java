package com.ms.Comunity.Service;

import com.ms.Comunity.Model.CommunityModel;
import com.ms.Comunity.Repository.CommunityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommunityService {

    private final CommunityRepository communityRepository;

    public CommunityModel createCommunity(String name, String description, String creatorUsername) {

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

        return communityRepository.save(community);
    }

    public CommunityModel joinCommunity(Long userId, String accessCode) {

        CommunityModel community = communityRepository.findByAccessCode(accessCode)
                .orElseThrow(() -> new RuntimeException("Código de acceso inválido o comunidad no encontrada"));

        if (!community.getMemberIds().contains(userId)) {
            community.getMemberIds().add(userId);
            community.setMemberCount(community.getMemberCount() + 1);
            return communityRepository.save(community);
        } else {
            throw new RuntimeException("El usuario ya pertenece a esta comunidad");
        }
    }
}
