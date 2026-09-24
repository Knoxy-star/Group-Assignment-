package zw.ac.uz.dpdms.fire.repository;

import zw.ac.uz.dpdms.fire.entity.FireAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FireAuditLogRepository extends JpaRepository<FireAuditLog, Long> {
    List<FireAuditLog> findByIncidentIdOrderByTimestampAsc(Long incidentId);
}
