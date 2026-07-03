package com.ms.Comunity.Service;

import com.ms.Comunity.Client.UserClient;
import com.ms.Comunity.DTOs.*;
import com.ms.Comunity.Model.CommunityModel;
import com.ms.Comunity.Repository.CommunityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio central para la gestión de las comunidades.
 * <p>
 * Se encarga de la persistencia de los grupos, la validación estricta de códigos de acceso privado,
 * el conteo de miembros y la integración síncrona con el microservicio ms-User para componer
 * la información del creador en tiempo real mediante el patrón API Composition.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CommunityService {

    private final CommunityRepository communityRepository;
    private final UserClient userClient;

    /**
     * Crea una nueva comunidad y asigna al creador como el primer miembro de esta.
     *
     * @param dto DTO de creación con el nombre, descripción y código de acceso.
     * @param creatorId Identidad validada del creador obtenida del JWT.
     * @param token Token de seguridad.
     * @return La comunidad mapeada en formato DTO.
     */
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

    /**
     * Permite a un nuevo usuario integrarse a una comunidad validando sus credenciales de ingreso.
     *
     * @param communityId Identificador del foro.
     * @param joinDto Estructura con el código de acceso a validar.
     * @param userId Identificador del nuevo postulante
     * @param token Token de seguridad
     * @return Comunidad actualizada con el nuevo conteo de miembros.
     */
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

    /**
     * Devuelve el catálogo completo de comunidades activas.
     *
     * @param token Token de seguridad para consultar usuarios.
     * @return Lista compuesta de comunidades.
     */
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

    /**
     * Obtiene la información pública del creador de la comunidad desde ms-User.
     * Garantiza que la consulta del catálogo de comunidades no falle si el servicio de usuarios cae.
     *
     * @param userId Identificador del usuario creador.
     * @param token Token de autorización.
     * @return UserDTO correspondiente.
     */
    private UserDTO obtenerUsuario(Long userId, String token) {
        try {
            return userClient.obtenerUsuarioPorId(userId, token);
        } catch (Exception e) {
            log.warn("Fallo al resolver identidad del creador ID {} en ms-User: {}", userId, e.getMessage());
            return new UserDTO(userId, "Usuario Desconocido", "Alias No Disponible");
        }
    }

    /**
     * Transforma la entidad interna en un DTO para exponer en la API,
     * acoplando los datos completos de su creador.
     *
     * @param model Entidad CommunityModel.
     * @param creator DTO del creador de la comunidad.
     * @return CommunityResponseDTO estructurado.
     */
    private CommunityResponseDTO construirResponseDTO(CommunityModel model, UserDTO creator) {
        return CommunityResponseDTO.builder()
                .id(model.getId())
                .name(model.getName())
                .description(model.getDescription())
                .creator(creator)
                .memberCount(model.getMemberCount())
                .createdAt(model.getCreatedAt())
                .build();
    }

}
