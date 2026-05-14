package com.ms.Report.Repository;

import com.ms.Report.Model.ReportModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<ReportModel, Long> {

    List<ReportModel> findByStatus(String status);

    List<ReportModel> findByReportedEntityTypeAndReportedEntityId(String type, Long id);
}
