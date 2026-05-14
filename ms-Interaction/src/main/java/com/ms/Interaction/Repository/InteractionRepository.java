package com.ms.Interaction.Repository;

import com.ms.Interaction.Model.InteractionModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InteractionRepository extends JpaRepository<InteractionModel, Long> {

    Optional<InteractionModel> findByUsernameAndEntityTypeAndEntityId(String username,
                                                                      String entityType, Long entityId);
}
