package zw.ac.uz.dpdms.drought.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import zw.ac.uz.dpdms.drought.entity.DroughtAuditLog;

import java.util.List;

public interface DroughtAuditLogRepository extends JpaRepository<DroughtAuditLog, Long> {
    List<DroughtAuditLog> findByIncidentIdOrderByTimestampAsc(Long incidentId);
}
