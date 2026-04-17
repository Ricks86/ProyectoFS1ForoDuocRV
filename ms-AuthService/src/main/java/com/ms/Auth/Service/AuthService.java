package com.ms.Auth.Service;


import com.ms.Auth.Model.AuthRequestDTO;
import com.ms.Auth.Model.RegisterRequestDTO;
import com.ms.Auth.Model.TokenResponseDTO;
import com.ms.Auth.Model.UserAuth;
import com.ms.Auth.Repository.UserAuthRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final UserAuthRepository userAuthRepository;

    @Transactional
    public UserAuth register( RegisterRequestDTO userAuth) {
        log.info("Registrando usario {}", userAuth.getNombreUser());

        if (userAuthRepository.existsByNombreUser(userAuth.getNombreUser())) {
            log.warn(userAuth.getNombreUser() + " Ya existe");
            throw new RuntimeException("El nombre de usuario ya existe");
        }

        if (userAuthRepository.existsByEmail(userAuth.getEmail())) {
            log.warn(userAuth.getEmail() + " Ya existe");
            throw new RuntimeException("El email ya está en usp");
        }

        UserAuth nuevo = UserAuth.builder()
                .nombreUser(userAuth.getNombreUser())
                .email(userAuth.getEmail())
                .password(userAuth.getPassword())
                .build();

        UserAuth guardado = userAuthRepository.save(nuevo);
        log.info("Registrando usario {}", userAuth.getNombreUser());
        return guardado;
    }

    public TokenResponseDTO login(AuthRequestDTO AuthRequest){
        log.info("Login usario {}", AuthRequest.getNombreUser());

        UserAuth user = userAuthRepository.findByNombreUser(AuthRequest.getNombreUser())
                .orElseThrow(() -> {
                    log.error("usuario no encontrado: {}", AuthRequest.getNombreUser());
                    return new RuntimeException("Usuario no encontrado");
                });

        if (!user.getPassword().equals(AuthRequest.getPassword())){
            log.error("Contraseña incorrecta para el usuario: {}", AuthRequest.getNombreUser());
            throw new RuntimeException("Contraseña incorrecta");
        }

        log.info("Login exitoso para el usario: {}", AuthRequest.getNombreUser());

        return TokenResponseDTO.builder()
                .token("token-fake-para-" + user.getNombreUser())
                .type("Bearer")
                .username(user.getNombreUser())
                .build();
    }

}
