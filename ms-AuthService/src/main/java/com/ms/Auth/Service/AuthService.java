package com.ms.Auth.Service;


import com.ms.Auth.Model.LoginRequestDTO;
import com.ms.Auth.Model.RegisterRequestDTO;
import com.ms.Auth.Model.TokenResponseDTO;
import com.ms.Auth.Model.UserAuth;
import com.ms.Auth.Repository.UserAuthRepository;
import com.ms.Auth.Security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.mvc.method.AbstractHandlerMethodAdapter;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final UserAuthRepository userAuthRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final WebClient.Builder webClientBuilder;

    @Transactional
    public String register( RegisterRequestDTO userAuth) {

        if (userAuthRepository.existsByNombreUser(userAuth.getNombreUser())) {
            throw new RuntimeException("El nombre de usuario ya existe");
        }

        if (userAuthRepository.existsByEmail(userAuth.getEmail())) {
            throw new RuntimeException("El email ya está en uso");
        }

        UserAuth nuevo = UserAuth.builder()
                .nombreUser(userAuth.getNombreUser())
                .email(userAuth.getEmail())
                .password(passwordEncoder.encode(userAuth.getPassword()))
                .build();

        userAuthRepository.save(nuevo);

        Map<String, Object> initData = new HashMap<>();
        initData.put("authId", nuevo.getId()); // El ID que te devolvió la BD de Auth
        initData.put("username", nuevo.getNombreUser());
        initData.put("email", nuevo.getEmail());

        webClientBuilder.build()
                .post()
                .uri("http://localhost:8082/users/init")
                .bodyValue(initData)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
        return "Usuario '" + nuevo.getNombreUser() + "' registrado con éxito en el sistema.";
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
                .build();
    }

}
