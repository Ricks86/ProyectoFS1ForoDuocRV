package com.ms.Comunity.Controller;

import com.ms.Comunity.Model.CommunityCreateDTO;
import com.ms.Comunity.Model.CommunityJoinDTO;
import com.ms.Comunity.Model.CommunityModel;
import com.ms.Comunity.Service.CommunityService;
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
@RequestMapping("/api/communities")
@RequiredArgsConstructor
@Slf4j
public class CommunityController {

    private final CommunityService communityService;

    @PostMapping("/create")
    public ResponseEntity<CommunityModel> createCommunity(@Valid @RequestBody CommunityCreateDTO request) {
        log.info("Peticion para crear comunidad: {}", request.getName());

        CommunityModel newCommunity = communityService.createCommunity(
                request.getName(),
                request.getDescription(),
                request.getCreatorUsername()

        );
        return new ResponseEntity<>(newCommunity, HttpStatus.CREATED);
    }

    @PostMapping("/join")
    public ResponseEntity<CommunityModel> joinCommunity(@Valid @RequestBody CommunityJoinDTO request) {
        log.info("Peticion del usuario con ID {} para unirse con codigo: {}", request.getUserId(), request.getAccessCode());

        CommunityModel updatedCommunity = communityService.joinCommunity(
                request.getUserId(),
                request.getAccessCode()
        );
        return ResponseEntity.ok(updatedCommunity);
    }
}