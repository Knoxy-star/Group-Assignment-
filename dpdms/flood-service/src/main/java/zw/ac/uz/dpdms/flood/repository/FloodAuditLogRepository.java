package zw.ac.uz.dpdms.flood.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import zw.ac.uz.dpdms.flood.entity.FloodAuditLog;

import java.util.List;

public interface FloodAuditLogRepository extends JpaRepository<FloodAuditLog, Long> {
    List<FloodAuditLog> findByIncidentIdOrderByTimestampAsc(Long incidentId);
}
