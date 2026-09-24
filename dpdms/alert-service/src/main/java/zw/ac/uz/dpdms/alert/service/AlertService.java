package zw.ac.uz.dpdms.alert.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zw.ac.uz.dpdms.alert.config.AlertProperties;
import zw.ac.uz.dpdms.alert.dto.AlertResponse;
import zw.ac.uz.dpdms.alert.entity.Alert;
import zw.ac.uz.dpdms.alert.entity.DeliveryStatus;
import zw.ac.uz.dpdms.alert.event.IncidentApprovedEvent;
import zw.ac.uz.dpdms.alert.notify.AlertNotifier;
import zw.ac.uz.dpdms.alert.notify.DeliveryResult;
import zw.ac.uz.dpdms.alert.repository.AlertRepository;
import zw.ac.uz.dpdms.common.AccessDeniedException;
import zw.ac.uz.dpdms.common.RequestContext;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class AlertService {

    private static final Logger log = LoggerFactory.getLogger(AlertService.class);
    private static final DateTimeFormatter WHEN = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    private final AlertRepository repository;
    private final AlertNotifier notifier;
    private final AlertProperties props;

    public AlertService(AlertRepository repository, AlertNotifier notifier, AlertProperties props) {
        this.repository = repository;
        this.notifier = notifier;
        this.props = props;
    }

    /** Called by the RabbitMQ listener for every "incident approved" event. */
    @Transactional
    public void handle(IncidentApprovedEvent event) {
        if (event.hazard() == null || event.incidentId() == null || event.severity() == null) {
            // Malformed message: log and drop, don't let it be redelivered forever.
            log.warn("Ignoring malformed alert event (hazard, incidentId and severity are required): {}", event);
            return;
        }
        if (repository.existsByHazardAndIncidentId(event.hazard(), event.incidentId())) {
            log.info("Alert for {} incident {} already exists - duplicate delivery ignored",
                    event.hazard(), event.incidentId());
            return;
        }

        String message = formatMessage(event);

        DeliveryResult result;
        if (event.severity().ordinal() < props.minSeverity().ordinal()) {
            result = new DeliveryResult(DeliveryStatus.SUPPRESSED,
                    "Severity " + event.severity() + " is below the alert threshold " + props.minSeverity());
        } else {
            result = notifier.send(message);
        }

        Alert alert = new Alert();
        alert.setHazard(event.hazard());
        alert.setIncidentId(event.incidentId());
        alert.setWard(event.ward());
        alert.setDistrict(event.district());
        alert.setProvince(event.province());
        alert.setSeverity(event.severity());
        alert.setOccurredAt(event.occurredAt());
        alert.setSummary(event.summary());
        alert.setMessage(message);
        alert.setChannel(notifier.channelName());
        alert.setDeliveryStatus(result.status());
        alert.setDeliveryDetail(result.detail());
        repository.save(alert);

        log.info("Recorded alert for {} incident {} - {}", event.hazard(), event.incidentId(), result.status());
    }

    /**
     * RBAC for viewing alerts:
     *  - WARD_RECORDER: no access
     *  - PROVINCIAL_SUPERVISOR: only their own hazard's alerts
     *  - NATIONAL_VIEWER / PROVINCIAL_ADMIN: all alerts
     */
    @Transactional(readOnly = true)
    public List<AlertResponse> list(RequestContext ctx) {
        List<Alert> alerts;
        if (ctx.isRecorder()) {
            throw new AccessDeniedException("Ward recorders cannot view alerts");
        } else if (ctx.isSupervisor()) {
            if (ctx.hazard() == null) {
                throw new AccessDeniedException("Supervisor has no hazard assigned");
            }
            alerts = repository.findByHazardOrderByReceivedAtDesc(ctx.hazard());
        } else if (ctx.isNationalViewer() || ctx.isProvincialAdmin()) {
            alerts = repository.findAllByOrderByReceivedAtDesc();
        } else {
            throw new AccessDeniedException("Role " + ctx.role() + " cannot view alerts");
        }
        return alerts.stream().map(AlertResponse::from).toList();
    }

    private String formatMessage(IncidentApprovedEvent e) {
        StringBuilder sb = new StringBuilder();
        sb.append("DPDMS ALERT: ").append(e.hazard().name().replace('_', ' '))
          .append(" (").append(e.severity()).append(")");
        if (e.ward() != null) {
            sb.append(" in ").append(e.ward());
        }
        if (e.district() != null) {
            sb.append(", ").append(e.district());
        }
        if (e.province() != null) {
            sb.append(", ").append(e.province());
        }
        sb.append(".");
        if (e.occurredAt() != null) {
            sb.append(" Occurred ").append(e.occurredAt().format(WHEN)).append(".");
        }
        if (e.summary() != null && !e.summary().isBlank()) {
            sb.append(" ").append(e.summary().trim());
        }
        sb.append(" [Incident #").append(e.incidentId()).append("]");
        String text = sb.toString();
        return text.length() <= 1000 ? text : text.substring(0, 1000);
    }
}
