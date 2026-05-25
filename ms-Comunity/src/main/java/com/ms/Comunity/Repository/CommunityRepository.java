package com.ms.Comunity.Repository;

import com.ms.Comunity.Model.CommunityModel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommunityRepository extends JpaRepository<CommunityModel, Long> {
    boolean existsByName(@NotBlank(message = "El nombre de la comunidad es obligatorio") @Size(min = 3, max = 50) String name);
}
