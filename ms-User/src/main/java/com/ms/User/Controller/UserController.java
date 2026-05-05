package com.ms.User.Controller;

import com.ms.User.Model.UserModel;
import com.ms.User.Model.UserProfileDTO;
import com.ms.User.Model.UserUpdateDTO;
import com.ms.User.Service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @PostMapping("/{username}/init")
    public ResponseEntity<UserModel> initProfile(@PathVariable String username) {
        log.info("Petición para inicializar perfil: {}", username);
        UserModel user = userService.createInitialProfile(username);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }


    @GetMapping("/{username}")
    public ResponseEntity<UserProfileDTO> getProfile(@PathVariable String username) {
        log.info("Petición GET perfil para: {}", username);
        UserProfileDTO profile = userService.getProfile(username);
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/{username}")
    public ResponseEntity<UserProfileDTO> updateProfile(
            @PathVariable String username,
            @Valid @RequestBody UserUpdateDTO updateDTO) {
        log.info("Petición PUT perfil para: {}", username);
        UserProfileDTO updated = userService.updateProfile(username, updateDTO);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{username}/reputation")
    public ResponseEntity<Void> updateReputation(
            @PathVariable String username,
            @RequestParam int points) {
        log.info("Petición para actualizar reputación a: {}", username);
        userService.addReputation(username, points);
        return ResponseEntity.ok().build();
    }
}
