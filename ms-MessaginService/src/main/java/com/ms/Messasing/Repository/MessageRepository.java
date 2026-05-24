package com.ms.Messasing.Repository;

import com.ms.Messasing.Model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("SELECT m.idEmisor, " +
            "SUM(CASE WHEN m.leido = false THEN 1L ELSE 0L END), " +
            "MAX(m.fechaEnvio) " +
            "FROM Message m WHERE m.idReceptor = :idLogueado " +
            "GROUP BY m.idEmisor " +
            "ORDER BY MAX(m.fechaEnvio) DESC")
    List<Object[]> getResumenBandeja(@Param("idLogueado") Long idLogueado);

    @Query("SELECT m FROM Message m WHERE " +
            "(m.idEmisor = :idLogueado AND m.idReceptor = :idOtroUsuario) OR " +
            "(m.idEmisor = :idOtroUsuario AND m.idReceptor = :idLogueado) " +
            "ORDER BY m.fechaEnvio ASC")
    List<Message> findConversacionCompleta(@Param("idLogueado") Long idLogueado,
                                           @Param("idOtroUsuario") Long idOtroUsuario);

    @Modifying
    @Query("UPDATE Message m SET m.leido = true WHERE m.idEmisor = :idOtroUsuario AND m.idReceptor = :idLogueado AND m.leido = false")
    void marcarMensajesComoLeidos(@Param("idOtroUsuario") Long idOtroUsuario, @Param("idLogueado") Long idLogueado);
}