package com.ms.Auth.Controller;

import com.ms.Auth.Model.AuthRequestDTO;
import com.ms.Auth.Model.RegisterRequestDTO;
import com.ms.Auth.Model.TokenResponseDTO;
import com.ms.Auth.Model.UserAuth;
import com.ms.Auth.Service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<UserAuth> register(@Valid @RequestBody RegisterRequestDTO registro){
        log.info("Peticion de registro para: {}", registro.getNombreUser());
        UserAuth nuevo = authService.register(registro);

        return new ResponseEntity<>(nuevo, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDTO> login(@Valid @RequestBody AuthRequestDTO auth){
        log.info("Peticion de login para: {}", auth.getNombreUser());
        TokenResponseDTO token = authService.login(auth);

        return ResponseEntity.ok(token);
    }

}
