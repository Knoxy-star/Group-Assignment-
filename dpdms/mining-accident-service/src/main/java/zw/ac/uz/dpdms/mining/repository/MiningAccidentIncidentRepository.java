package zw.ac.uz.dpdms.mining.repository;

import zw.ac.uz.dpdms.common.IncidentStatus;
import zw.ac.uz.dpdms.mining.entity.MiningAccidentIncident;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MiningAccidentIncidentRepository extends JpaRepository<MiningAccidentIncident, Long> {

    // WARD_RECORDER's own submissions, any status ("my submissions" list)
    List<MiningAccidentIncident> findByReporterIdOrderByCreatedAtDesc(Long reporterId);

    // PROVINCIAL_SUPERVISOR's / PROVINCIAL_ADMIN's queue, filterable by status
    List<MiningAccidentIncident> findByStatusOrderByCreatedAtDesc(IncidentStatus status);

    // everything, for supervisors/admins browsing full history
    List<MiningAccidentIncident> findAllByOrderByCreatedAtDesc();
}
