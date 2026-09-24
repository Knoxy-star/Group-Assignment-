package zw.ac.uz.dpdms.drought.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import zw.ac.uz.dpdms.common.IncidentStatus;
import zw.ac.uz.dpdms.drought.entity.DroughtIncident;

import java.util.List;

public interface DroughtIncidentRepository extends JpaRepository<DroughtIncident, Long> {

    // WARD_RECORDER's own submissions, any status ("my submissions" list)
    List<DroughtIncident> findByReporterIdOrderByCreatedAtDesc(Long reporterId);

    // PROVINCIAL_SUPERVISOR's / PROVINCIAL_ADMIN's queue, filterable by status
    List<DroughtIncident> findByStatusOrderByCreatedAtDesc(IncidentStatus status);

    // everything, for supervisors/admins browsing full history
    List<DroughtIncident> findAllByOrderByCreatedAtDesc();
}
