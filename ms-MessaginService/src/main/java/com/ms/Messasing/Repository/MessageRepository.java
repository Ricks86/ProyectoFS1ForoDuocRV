package com.ms.Messasing.Repository;

import com.ms.Messasing.Model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByIdEmisorOrIdReceptorOrderByFechaEnvioDesc(Long idEmisor, Long idReceptor);

    List<Message> findByIdEmisorAndIdReceptorOrIdReceptorAndIdEmisorOrderByFechaEnvioDesc(Long eId1, Long rId1, Long eId2, Long rId2);

    long countByIdReceptorAndLeidoFalse(Long idReceptor);
}
