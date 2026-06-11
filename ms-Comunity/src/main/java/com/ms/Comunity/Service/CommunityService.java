package com.ms.Comunity.Service;

import com.ms.Comunity.Client.UserClient;
import com.ms.Comunity.Model.*;
import com.ms.Comunity.Repository.CommunityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommunityService {

    private final CommunityRepository communityRepository;
    private final UserClient userClient;

    @Transactional
    public CommunityResponseDTO createCommunity(CommunityCreateDTO dto, Long creatorId, String token) {
        if (communityRepository.existsByName(dto.getName())) {
            throw new IllegalArgumentException("La comunidad ya existe.");
        }

        CommunityModel community = CommunityModel.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .creatorId(creatorId)
                .communityAccess(dto.getCommunityAccess())
                .memberCount(1)
                .build();

        community.getMemberIds().add(creatorId);

        CommunityModel saved = communityRepository.save(community);
        UserDTO creatorDto = obtenerUsuario(creatorId, token);

        return construirResponseDTO(saved, creatorDto);
    }

    @Transactional
    public CommunityResponseDTO joinCommunity(Long communityId, CommunityJoinDTO joinDto, Long userId, String token) {
        CommunityModel community = communityRepository.findById(communityId)
                .orElseThrow(() -> new RuntimeException("La comunidad no existe"));

        if (!community.getCommunityAccess().equals(joinDto.getAccessCode())) {
            throw new RuntimeException("Código de acceso incorrecto");
        }

        if (community.getMemberIds().contains(userId)) {
            throw new RuntimeException("Ya eres miembro de esta comunidad");
        }

        community.getMemberIds().add(userId);
        community.setMemberCount(community.getMemberCount() + 1);

        CommunityModel updated = communityRepository.save(community);

        UserDTO creatorDto = obtenerUsuario(community.getCreatorId(), token);

        return construirResponseDTO(updated, creatorDto);
    }
    @Transactional(readOnly = true)
    public List<CommunityResponseDTO> getAllCommunities(String token) {
        List<CommunityModel> communities = communityRepository.findAll();

        Map<Long, UserDTO> userCache = new HashMap<>();

        return communities.stream()
                .map(c -> {
                    UserDTO creatorDto = userCache.computeIfAbsent(c.getCreatorId(),
                            id -> obtenerUsuario(id, token));
                    return construirResponseDTO(c, creatorDto);
                })
                .toList();
    }

    private UserDTO obtenerUsuario(Long userId, String token) {
        try {
            return userClient.obtenerUsuarioPorId(userId, token);
        } catch (Exception e) {
            log.warn("Fallo al resolver identidad del creador ID {} en ms-User: {}", userId, e.getMessage());
            return new UserDTO(userId, "Usuario Desconocido", "Alias No Disponible");
        }
    }

    private CommunityResponseDTO construirResponseDTO(CommunityModel model, UserDTO creator) {
        return CommunityResponseDTO.builder()
                .id(model.getId())
                .name(model.getName())
                .description(model.getDescription())
                .creator(creator)
                .build();
    }

}
