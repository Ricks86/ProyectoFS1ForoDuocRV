package com.ms.User.Controller;

import com.ms.User.Model.*;
import com.ms.User.Security.JwtUtil;
import com.ms.User.Service.AuditService;
import com.ms.User.Service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final AuditService auditoriaService;

    @PostMapping("/init")
    public ResponseEntity<UserModel> initProfile(@Valid @RequestBody UserInitDTO request) {

        UserModel user = userService.createInitialProfile(
                request.getAuthId(),
                request.getUsername(),
                request.getEmail()
        );
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }


    @GetMapping("/perfil/{username}")
    public ResponseEntity<UserProfileDTO> getProfile(@PathVariable String username) {
        log.info("Petición GET perfil para: {}", username);
        UserProfileDTO profile = userService.getProfile(username);
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/actualizar/{username}")
    public ResponseEntity<UserProfileDTO> updateProfile(
            @PathVariable String username,
            @Valid @RequestBody UserUpdateDTO updateDTO,
            @RequestHeader("Authorization") String token) {

        Long idUsuarioLogueado = jwtUtil.extractUserId(token);

        log.info("Petición PUT perfil para: {}", username);

        UserProfileDTO updated = userService.updateProfile(username, updateDTO, idUsuarioLogueado);

        auditoriaService.registrarLog(
                idUsuarioLogueado,
                "UPDATE_PROFILE",
                "El usuario actualizó su biografía o avatar en el sistema"
        );

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

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> obtenerPorId(@PathVariable Long id) {
        UserDTO usuario = userService.obtenerUsuarioDtoPorId(id);
        return ResponseEntity.ok(usuario);
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<UserDTO> obtenerPorUsername(@PathVariable String username) {
        UserDTO usuario = userService.obtenerUsuarioDtoPorUsername(username);
        return ResponseEntity.ok(usuario);
    }
}
