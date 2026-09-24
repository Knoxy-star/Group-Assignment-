package zw.ac.uz.dpdms.zoonotic.repository;

import zw.ac.uz.dpdms.zoonotic.entity.ZoonoticAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ZoonoticAuditLogRepository extends JpaRepository<ZoonoticAuditLog, Long> {
    List<ZoonoticAuditLog> findByIncidentIdOrderByTimestampAsc(Long incidentId);
}
