package com.ms.User.Service;

import com.ms.User.DTOs.UserDTO;
import com.ms.User.Model.UserModel;
import com.ms.User.DTOs.UserProfileDTO;
import com.ms.User.DTOs.UserUpdateDTO;
import com.ms.User.Repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Servicio central para la gestión de perfiles de usuario.
 * <p>
 * Se encarga de la persistencia de los datos públicos (biografía, alias, avatar)
 * y de servir como fuente de verdad y consolidación para el resto de los microservicios
 * que requieren información del usuario mediante el patrón API Composition.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * Inicializa un nuevo perfil de usuario en el sistema a partir de los datos básicos.
     *
     * @param authId Identificador único proveniente del MS de Autenticación.
     * @param username Nombre de usuario registrado.
     * @param email Correo electrónico del usuario.
     * @return El modelo del usuario recién persistido en base de datos.
     */
    @Transactional
    public UserModel createInitialProfile(Long authId, String username, String email) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("El perfil de usuario ya existe");
        }

        UserModel newUser = UserModel.builder()
                .authId(authId)
                .username(username)
                .email(email)
                .build();

        return userRepository.save(newUser);
    }

    /**
     * Consulta y mapea los datos públicos de un perfil dado su nombre de usuario.
     *
     * @param username Nombre de usuario a consultar.
     * @return UserProfileDTO con la información formateada para su exposición pública.
     */
    public UserProfileDTO getProfile(String username) {

        UserModel user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    return new RuntimeException("Usuario no encontrado");
                });

        return UserProfileDTO.builder()
                .username(user.getUsername())
                .bio(user.getBio())
                .avatarUrl(user.getAvatarUrl())
                .reputationLevel(user.getReputationLevel())
                .followersCount(user.getFollowersCount())
                .followingCount(user.getFollowingCount())
                .build();
    }

    /**
     * Actualiza la información personal de un usuario garantizando que sea el dueño de la cuenta
     *
     * @param username Nombre de usuario objetivo a modificar.
     * @param updateData Objeto DTO que contiene los nuevos datos.
     * @param idUsuarioLogueado Identificador extraído del JWT para validar autorización.
     * @return UserProfileDTO con los datos ya actualizados.
     */
    @Transactional
    public UserProfileDTO updateProfile(String username, UserUpdateDTO updateData, Long idUsuarioLogueado) {

        UserModel user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!user.getAuthId().equals(idUsuarioLogueado)) {
            throw new RuntimeException("Acceso denegado: No tienes permisos para modificar el perfil de otro usuario");
        }

        if (updateData.getBio() != null ) user.setBio(updateData.getBio());
        if (updateData.getAvatarUrl() != null) user.setAvatarUrl(updateData.getAvatarUrl());

        UserModel saved = userRepository.save(user);

        return UserProfileDTO.builder()
                .username(saved.getUsername())
                .bio(saved.getBio())
                .avatarUrl(saved.getAvatarUrl())
                .reputationLevel(saved.getReputationLevel())
                .followersCount(saved.getFollowersCount())
                .followingCount(saved.getFollowingCount())
                .build();
    }

    /**
     * Modifica el nivel de reputación de un usuario en la plataforma.
     *
     * @param username Nombre del usuario a actualizar.
     * @param points   Cantidad de puntos a sumar (pueden ser valores negativos).
     */
    @Transactional
    public void addReputation(String username, int points) {
        log.info("Sumando {} puntos de reputacion a {}", points, username);

        UserModel user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        user.setReputationLevel(user.getReputationLevel() + points);
        userRepository.save(user);
    }

    //Dtos


    /**
     * Devuelve los datos básicos de un usuario buscando por su ID de Auth.
     * Diseñado para responder a las consultas internas (Feign Clients) de los demás microservicios.
     *
     * @param id Identificador de autenticación del usuario.
     * @return UserDTO con la información esencial para mostrar al autor.
     */
    @Transactional(readOnly = true)
    public UserDTO obtenerUsuarioDtoPorId(Long id) {
        UserModel usuario = userRepository.findByAuthId(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

        return new UserDTO(usuario.getAuthId(), usuario.getUsername(), usuario.getAlias());
    }


    /**
     * Devuelve los datos básicos de un usuario buscando por su username.
     * Diseñado para integraciones y búsquedas de perfiles desde otros módulos.
     *
     * @param username Nombre de usuario registrado.
     * @return UserDTO con la información pública del usuario.
     */
    @Transactional(readOnly = true)
    public UserDTO obtenerUsuarioDtoPorUsername(String username) {
        UserModel usuario = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con username: " + username));

        return new UserDTO(usuario.getAuthId(), usuario.getUsername(), usuario.getAlias());
    }

}


