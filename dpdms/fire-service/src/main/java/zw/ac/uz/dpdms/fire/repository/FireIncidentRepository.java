package zw.ac.uz.dpdms.fire.repository;

import zw.ac.uz.dpdms.common.IncidentStatus;
import zw.ac.uz.dpdms.fire.entity.FireIncident;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FireIncidentRepository extends JpaRepository<FireIncident, Long> {

    // WARD_RECORDER's own submissions, any status ("my submissions" list)
    List<FireIncident> findByReporterIdOrderByCreatedAtDesc(Long reporterId);

    // PROVINCIAL_SUPERVISOR's / PROVINCIAL_ADMIN's queue, filterable by status
    List<FireIncident> findByStatusOrderByCreatedAtDesc(IncidentStatus status);

    // everything, for supervisors/admins browsing full history
    List<FireIncident> findAllByOrderByCreatedAtDesc();
}
