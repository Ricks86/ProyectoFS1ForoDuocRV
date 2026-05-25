package com.ms.Interaction.Repository;

import com.ms.Interaction.Model.InteractionModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InteractionRepository extends JpaRepository<InteractionModel, Long> {

    Optional<InteractionModel> findByEntityIdAndUserIdAndEntityType(Long entityId, Long userId, String entityType);

    List<InteractionModel> findByEntityIdAndEntityType(Long entityId, String entityType);
}

