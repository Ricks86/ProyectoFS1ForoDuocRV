package com.ms.Comunity.Repository;

import com.ms.Comunity.Model.CommunityModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommunityRepository extends JpaRepository<CommunityModel, Long> {

    Optional<CommunityModel> findByAccessCode(String accessCode);

    Optional<CommunityModel> findByName(String name);

}
