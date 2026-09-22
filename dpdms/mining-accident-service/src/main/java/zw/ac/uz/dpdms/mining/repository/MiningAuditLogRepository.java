package zw.ac.uz.dpdms.mining.repository;

import zw.ac.uz.dpdms.mining.entity.MiningAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MiningAuditLogRepository extends JpaRepository<MiningAuditLog, Long> {
    List<MiningAuditLog> findByIncidentIdOrderByTimestampAsc(Long incidentId);
}
