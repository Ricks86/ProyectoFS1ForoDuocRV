package com.ms.User.Repository;


import com.ms.User.Model.UserModel;
import org.apache.catalina.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserModel, Long> {

    Optional<UserModel> findByUserName(String username);

    Boolean existsByUsername(String username);
}
