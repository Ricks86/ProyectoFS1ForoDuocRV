package com.ms.Auth.Service;


import com.ms.Auth.Client.UserClient;
import com.ms.Auth.Model.*;
import com.ms.Auth.Repository.UserAuthRepository;
import com.ms.Auth.Security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final UserAuthRepository userAuthRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserClient userClient;

    @Transactional
    public RegisterResponseDTO register( RegisterRequestDTO userAuth) {

        if (userAuthRepository.existsByNombreUser(userAuth.getNombreUser())) {
            log.info("Error: nombre de usuario existente");
            throw new RuntimeException("El nombre de usuario ya existe");
        }

        if (userAuthRepository.existsByEmail(userAuth.getEmail())) {
            log.info("el email ya está en uso");
            throw new RuntimeException("El email ya está en uso");
        }

        UserAuth nuevo = UserAuth.builder()
                .nombreUser(userAuth.getNombreUser())
                .email(userAuth.getEmail())
                .password(passwordEncoder.encode(userAuth.getPassword()))
                .build();

        userAuthRepository.save(nuevo);

        Map<String, Object> initData = new HashMap<>();
        initData.put("authId", nuevo.getId());
        initData.put("username", nuevo.getNombreUser());
        initData.put("email", nuevo.getEmail());

        try {
            userClient.inicializarUsuario(initData);
            log.info("Perfil inicializado en ms-User para authId [{}]", nuevo.getId());
        } catch (Exception e) {
            log.error("Error al contactar ms-User para inicializar perfil. authId [{}]. Detalle: {}", nuevo.getId(), e.getMessage());
        }

        return new RegisterResponseDTO(
                nuevo.getId(),
                "Usuario '" + nuevo.getNombreUser() + "' registrado con éxito en el sistema."
        );
    }

    public TokenResponseDTO login(LoginRequestDTO request){

        UserAuth user = userAuthRepository.findByNombreUser(request.getNombreUser())
                .orElseThrow(() -> new RuntimeException("Credenciales Invalidas"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            throw new RuntimeException("Credenciales invalidas");
        }

        String token = jwtService.generateToken(user);

        return TokenResponseDTO.builder()
                .token(token)
                .username(user.getNombreUser())
                .usuarioId(user.getId())
                .build();
    }

}
