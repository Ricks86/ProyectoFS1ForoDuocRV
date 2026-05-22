package com.ms.Auth.Repository;

import com.ms.Auth.Model.UserAuth;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.swing.text.html.Option;
import java.util.Optional;

public interface UserAuthRepository extends JpaRepository<UserAuth, Long> {

    Optional<UserAuth> findByNombreUser(String nombreUser);

    Boolean existsByNombreUser(String nombreUser);
    Boolean existsByEmail(String email);


}
