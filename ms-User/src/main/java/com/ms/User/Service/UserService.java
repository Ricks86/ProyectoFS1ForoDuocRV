package com.ms.User.Service;

import com.ms.User.Model.UserModel;
import com.ms.User.Model.UserProfileDTO;
import com.ms.User.Model.UserUpdateDTO;
import com.ms.User.Repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserModel createInitialProfile(String username) {
        log.info("Registrando perfil inicial para : {}", username);

        if (userRepository.existsByUsername(username)) {
            log.warn("El perfil para {} ya existe", username);
            throw new RuntimeException("El perfil de usuario ya existe");
        }

        UserModel newUser = UserModel.builder()
                .username(username)
                .build();

        return userRepository.save(newUser);
    }

    public UserProfileDTO getProfile(String username) {
        log.info("Obteniendo perfil para: {}", username);

        UserModel user = userRepository.findByUserName(username)
                .orElseThrow(() -> {
                    log.error("Usuario no encontrado: {}", username);
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
    public UserProfileDTO updateProfile(String username, UserUpdateDTO updateData) {
        log.info("Actualizando perfil de: {}", username);

        UserModel user = userRepository.findByUserName(username)
                .orElseThrow(() -> {
                    log.error("Usuario no encontrado: {}", username);
                    return new RuntimeException("Usuario no encontrado");
                });

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

        UserModel user = userRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        user.setReputationLevel(user.getReputationLevel() + points);
        userRepository.save(user);
    }

}


