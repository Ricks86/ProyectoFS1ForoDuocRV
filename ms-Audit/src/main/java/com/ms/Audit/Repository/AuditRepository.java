package com.ms.Audit.Repository;

import com.ms.Audit.Model.AuditModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditRepository extends JpaRepository<AuditModel, Long> {

    List<AuditModel> findByNombreServicio(String nombreServicio);

    List<AuditModel> findByAccion(String accion);

    List<AuditModel> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    List<AuditModel> findByUserId(String userId);
}
