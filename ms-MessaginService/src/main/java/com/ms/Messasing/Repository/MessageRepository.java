package com.ms.Messasing.Repository;

import com.ms.Messasing.Model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("SELECT DISTINCT m.idEmisor FROM Message m WHERE m.idReceptor = :idLogueado")
    List<Long> findDistinctEmisoresByIdReceptor(@Param("idLogueado") Long idLogueado);

    @Query("SELECT m FROM Message m WHERE " +
            "(m.idEmisor = :idLogueado AND m.idReceptor = :idOtroUsuario) OR " +
            "(m.idEmisor = :idOtroUsuario AND m.idReceptor = :idLogueado) " +
            "ORDER BY m.fechaEnvio ASC")
    List<Message> findConversacionCompleta(@Param("idLogueado") Long idLogueado,
                                           @Param("idOtroUsuario") Long idOtroUsuario);
}