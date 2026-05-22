package com.ms.Audit.Repository;

import com.ms.Audit.Model.AuditModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface AuditRepository extends JpaRepository<AuditModel, Long> {

}
