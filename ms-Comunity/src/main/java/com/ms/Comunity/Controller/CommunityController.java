package com.ms.Comunity.Controller;

import com.ms.Comunity.DTOs.CommunityCreateDTO;
import com.ms.Comunity.DTOs.CommunityJoinDTO;
import com.ms.Comunity.DTOs.CommunityResponseDTO;
import com.ms.Comunity.Security.JwtUtil;
import com.ms.Comunity.Service.AuditService;
import com.ms.Comunity.Service.CommunityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/communities")
@RequiredArgsConstructor
@Slf4j
public class CommunityController {

    private final CommunityService communityService;
    private final JwtUtil jwtUtil;
    private final AuditService auditoriaService;

    @PostMapping
    public ResponseEntity<CommunityResponseDTO> createCommunity(
            @Valid @RequestBody CommunityCreateDTO request,
            @RequestHeader("Authorization") String token) {

        Long creatorId = jwtUtil.extractUserId(token);
        log.info("Petición para crear comunidad enviada por AuthID: {}", creatorId);

        CommunityResponseDTO response = communityService.createCommunity(request, creatorId, token);

        auditoriaService.registrarLog(
                creatorId,
                "CREATE_COMMUNITY",
                "Comunidad [" + request.getName() + "] creada con código de acceso privado."
        );

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/{communityId}/join")
    public ResponseEntity<CommunityResponseDTO> joinCommunity(
            @PathVariable Long communityId,
            @Valid @RequestBody CommunityJoinDTO joinRequest,
            @RequestHeader("Authorization") String token) {

        Long userId = jwtUtil.extractUserId(token);
        log.info("Usuario ID {} intentando unirse a comunidad ID {}", userId, communityId);

        CommunityResponseDTO response = communityService.joinCommunity(communityId, joinRequest, userId, token);

        auditoriaService.registrarLog(
                userId,
                "JOIN_COMMUNITY",
                "El usuario se ha unido a la comunidad ID [" + communityId + "]"
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<CommunityResponseDTO>> getAllCommunities(
            @RequestHeader("Authorization") String token) {
        List<CommunityResponseDTO> list = communityService.getAllCommunities(token);
        return ResponseEntity.ok(list);
    }

}