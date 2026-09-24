package zw.ac.uz.dpdms.alert.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import zw.ac.uz.dpdms.alert.entity.Alert;
import zw.ac.uz.dpdms.common.Hazard;

import java.util.List;

public interface AlertRepository extends JpaRepository<Alert, Long> {

    boolean existsByHazardAndIncidentId(Hazard hazard, Long incidentId);

    List<Alert> findAllByOrderByReceivedAtDesc();

    List<Alert> findByHazardOrderByReceivedAtDesc(Hazard hazard);
}
