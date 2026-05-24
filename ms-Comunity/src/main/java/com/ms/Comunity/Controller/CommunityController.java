package com.ms.Comunity.Controller;

import com.ms.Comunity.Model.CommunityCreateDTO;
import com.ms.Comunity.Model.CommunityJoinDTO;
import com.ms.Comunity.Model.CommunityModel;
import com.ms.Comunity.Security.JwtUtil;
import com.ms.Comunity.Service.CommunityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/communities")
@RequiredArgsConstructor
@Slf4j
public class CommunityController {

    private final CommunityService communityService;
    private final JwtUtil jwtUtil;

    @PostMapping("/create")
    public ResponseEntity<CommunityModel> createCommunity(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody CommunityCreateDTO request) {

        log.info("Petición para crear comunidad de forma segura: {}", request.getName());

        String jwt = token.substring(7);

        Long userId = jwtUtil.extractUserId(jwt);
        String username = jwtUtil.extractUsername(jwt);

        CommunityModel newCommunity = communityService.createCommunity(
                request.getName(),
                request.getDescription(),
                username,
                userId
        );
        return new ResponseEntity<>(newCommunity, HttpStatus.CREATED);
    }

    @PostMapping("/join")
    public ResponseEntity<CommunityModel> joinCommunity(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody CommunityJoinDTO request) {

        String jwt = token.substring(7);
        Long userId = jwtUtil.extractUserId(jwt);

        log.info("Petición del usuario con ID {} para unirse con código: {}", userId, request.getAccessCode());

        CommunityModel updatedCommunity = communityService.joinCommunity(
                userId,
                request.getAccessCode()
        );
        return ResponseEntity.ok(updatedCommunity);
    }

}