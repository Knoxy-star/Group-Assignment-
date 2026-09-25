package zw.ac.uz.dpdms.report.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import zw.ac.uz.dpdms.report.entity.ReportLog;

import java.util.List;

public interface ReportLogRepository extends JpaRepository<ReportLog, Long> {

    List<ReportLog> findTop50ByOrderByGeneratedAtDesc();
}
