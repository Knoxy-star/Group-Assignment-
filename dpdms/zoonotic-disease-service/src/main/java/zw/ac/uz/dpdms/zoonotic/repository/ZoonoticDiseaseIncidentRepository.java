package zw.ac.uz.dpdms.zoonotic.repository;

import zw.ac.uz.dpdms.common.IncidentStatus;
import zw.ac.uz.dpdms.zoonotic.entity.ZoonoticDiseaseIncident;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ZoonoticDiseaseIncidentRepository extends JpaRepository<ZoonoticDiseaseIncident, Long> {

    // WARD_RECORDER's own submissions, any status ("my submissions" list)
    List<ZoonoticDiseaseIncident> findByReporterIdOrderByCreatedAtDesc(Long reporterId);

    // PROVINCIAL_SUPERVISOR's / PROVINCIAL_ADMIN's queue, filterable by status
    List<ZoonoticDiseaseIncident> findByStatusOrderByCreatedAtDesc(IncidentStatus status);

    // everything, for supervisors/admins browsing full history
    List<ZoonoticDiseaseIncident> findAllByOrderByCreatedAtDesc();
}
