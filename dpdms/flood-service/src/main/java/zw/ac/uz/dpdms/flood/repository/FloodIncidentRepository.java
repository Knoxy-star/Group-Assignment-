package zw.ac.uz.dpdms.flood.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import zw.ac.uz.dpdms.common.IncidentStatus;
import zw.ac.uz.dpdms.flood.entity.FloodIncident;

import java.util.List;

public interface FloodIncidentRepository extends JpaRepository<FloodIncident, Long> {

    // WARD_RECORDER's own submissions, any status ("my submissions" list)
    List<FloodIncident> findByReporterIdOrderByCreatedAtDesc(Long reporterId);

    // PROVINCIAL_SUPERVISOR's / PROVINCIAL_ADMIN's queue, filterable by status
    List<FloodIncident> findByStatusOrderByCreatedAtDesc(IncidentStatus status);

    // everything, for supervisors/admins browsing full history
    List<FloodIncident> findAllByOrderByCreatedAtDesc();
}
