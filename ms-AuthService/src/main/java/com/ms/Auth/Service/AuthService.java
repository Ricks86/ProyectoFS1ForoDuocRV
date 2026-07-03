package com.ms.Auth.Service;


import com.ms.Auth.Client.UserClient;
import com.ms.Auth.DTOs.LoginRequestDTO;
import com.ms.Auth.DTOs.RegisterRequestDTO;
import com.ms.Auth.DTOs.RegisterResponseDTO;
import com.ms.Auth.DTOs.TokenResponseDTO;
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


/**
 * Servicio encargado de gestionar la lógica al crear usuarios y cuando estos inician sesión
 * Administra el registro de nuevas credenciales, la validación de accesos
 * y la emisión de tokens de seguridad JWT
 */

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final UserAuthRepository userAuthRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserClient userClient;

    /**
     * Registra un nuevo usuario en el sistema.
     * <p>
     * Valida de forma estricta que el nombre de usuario y el correo electrónico no existan previamente
     * tras la validación, encripta la contraseña utilizando BCrypt y persiste las credenciales
     * finalmente, ejecuta una llamada síncrona al microservicio ms-User para inicializar el perfil público
     * si ms-User no responde, la excepción es capturada localmente para garantizar que el registro
     * no haga un rollback y las credenciales se guarden con éxito.
     *
     * @param userAuth Objeto DTO que contiene el username, email y la contraseña cruda.
     * @return RegisterResponseDTO con el identificador único generado y un mensaje de confirmación.
     * @throws RuntimeException Si el nombre de usuario o el correo electrónico ya se encuentran en uso.
     */

    @Transactional
    public RegisterResponseDTO register(RegisterRequestDTO userAuth) {

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

    /**
     * Autentica a un usuario y genera su token de acceso.
     * <p>
     * Busca al usuario por su identificador en el repositorio y compara la contraseña
     * proporcionada en la petición con el hash BCrypt almacenado
     * si las credenciales coinciden, emite un token JWT firmado criptográficamente.
     *
     * @param request Objeto DTO que encapsula el nombre de usuario y la contraseña a validar.
     * @return TokenResponseDTO con el token JWT generado, el username y su ID de autorización.
     * @throws RuntimeException Si el usuario no existe en la base de datos o si la contraseña es incorrecta.
     */
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
