package com.ms.User.Service;

import com.ms.User.Model.UserDTO;
import com.ms.User.Model.UserModel;
import com.ms.User.Model.UserProfileDTO;
import com.ms.User.Model.UserUpdateDTO;
import com.ms.User.Repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

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

    @Transactional
    public void addReputation(String username, int points) {
        log.info("Sumando {} puntos de reputacion a {}", points, username);

        UserModel user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        user.setReputationLevel(user.getReputationLevel() + points);
        userRepository.save(user);
    }

    //Dtos

    @Transactional(readOnly = true)
    public UserDTO obtenerUsuarioDtoPorId(Long id) {
        UserModel usuario = userRepository.findByAuthId(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

        return new UserDTO(usuario.getAuthId(), usuario.getUsername(), usuario.getAlias());
    }

    @Transactional(readOnly = true)
    public UserDTO obtenerUsuarioDtoPorUsername(String username) {
        UserModel usuario = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con username: " + username));

        return new UserDTO(usuario.getAuthId(), usuario.getUsername(), usuario.getAlias());
    }

}


