package zw.ac.uz.dpdms.alert.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zw.ac.uz.dpdms.alert.config.AlertProperties;
import zw.ac.uz.dpdms.alert.dto.AlertResponse;
import zw.ac.uz.dpdms.alert.entity.Alert;
import zw.ac.uz.dpdms.alert.entity.AlertDelivery;
import zw.ac.uz.dpdms.alert.entity.DeliveryStatus;
import zw.ac.uz.dpdms.common.IncidentApprovedEvent;
import zw.ac.uz.dpdms.alert.notify.AlertNotifier;
import zw.ac.uz.dpdms.alert.notify.DeliveryResult;
import zw.ac.uz.dpdms.alert.notify.LoggingNotifier;
import zw.ac.uz.dpdms.alert.repository.AlertRepository;
import zw.ac.uz.dpdms.common.AccessDeniedException;
import zw.ac.uz.dpdms.common.RequestContext;
import zw.ac.uz.dpdms.common.Severity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class AlertService {

    private static final Logger log = LoggerFactory.getLogger(AlertService.class);
    private static final DateTimeFormatter WHEN = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    private final AlertRepository repository;
    private final List<AlertNotifier> notifiers;
    private final LoggingNotifier loggingNotifier;
    private final AlertProperties props;

    /** Spring injects every AlertNotifier bean (EmailNotifier, WhatsAppCloudNotifier, ...). */
    public AlertService(AlertRepository repository, List<AlertNotifier> notifiers,
                        LoggingNotifier loggingNotifier, AlertProperties props) {
        this.repository = repository;
        this.notifiers = notifiers;
        this.loggingNotifier = loggingNotifier;
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

        String reason = alertReason(event);
        String message = formatMessage(event, reason);

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
        alert.setAlertReason(reason != null ? reason : event.alertReason());

        if (reason == null) {
            alert.setChannel("NONE");
            alert.setDeliveryStatus(DeliveryStatus.SUPPRESSED);
            alert.setDeliveryDetail("Did not meet the " + event.hazard().name().replace('_', ' ')
                    + " alerting criteria"
                    + (event.alertReason() != null ? " (" + event.alertReason() + ")" : ""));
        } else {
            dispatch(alert, subject(event), message);
        }
        repository.save(alert);

        log.info("Recorded alert for {} incident {} - {}", event.hazard(), event.incidentId(),
                alert.getDeliveryStatus());
    }

    /**
     * Sends through every enabled channel, to every configured recipient,
     * recording one AlertDelivery row per attempt. If no channel is
     * enabled, writes to the application log instead.
     */
    private void dispatch(Alert alert, String subject, String message) {
        List<AlertNotifier> active = notifiers.stream().filter(AlertNotifier::isEnabled).toList();

        if (active.isEmpty()) {
            DeliveryResult r = loggingNotifier.write(message);
            alert.addDelivery(delivery(LoggingNotifier.CHANNEL, "application log", r));
            alert.setChannel(LoggingNotifier.CHANNEL);
            alert.setDeliveryStatus(r.status());
            alert.setDeliveryDetail(r.detail());
            return;
        }

        List<String> channels = new ArrayList<>();
        int attempts = 0;
        int sent = 0;
        for (AlertNotifier notifier : active) {
            channels.add(notifier.channelName());
            List<String> recipients = notifier.recipients();
            if (recipients.isEmpty()) {
                alert.addDelivery(delivery(notifier.channelName(), "(none configured)",
                        new DeliveryResult(DeliveryStatus.FAILED,
                                "Channel enabled but no recipients configured")));
                attempts++;
                continue;
            }
            for (String recipient : recipients) {
                DeliveryResult r;
                try {
                    r = notifier.send(recipient, subject, message);
                } catch (Exception e) {
                    // Notifiers shouldn't throw, but never let one channel
                    // stop the others or cause a RabbitMQ redelivery.
                    r = new DeliveryResult(DeliveryStatus.FAILED, String.valueOf(e.getMessage()));
                }
                alert.addDelivery(delivery(notifier.channelName(), notifier.displayRecipient(recipient), r));
                attempts++;
                if (r.status() == DeliveryStatus.SENT) {
                    sent++;
                }
            }
        }

        alert.setChannel(String.join(", ", channels));
        alert.setDeliveryStatus(attempts > 0 && sent == attempts ? DeliveryStatus.SENT : DeliveryStatus.FAILED);
        alert.setDeliveryDetail(sent + " of " + attempts + " deliveries succeeded");
    }

    private static AlertDelivery delivery(String channel, String recipient, DeliveryResult result) {
        AlertDelivery d = new AlertDelivery();
        d.setChannel(channel);
        d.setRecipient(recipient.length() <= 255 ? recipient : recipient.substring(0, 255));
        d.setStatus(result.status());
        String detail = result.detail();
        d.setDetail(detail == null || detail.length() <= 1000 ? detail : detail.substring(0, 1000));
        d.setAttemptedAt(LocalDateTime.now());
        return d;
    }

    private static String subject(IncidentApprovedEvent e) {
        return "DPDMS ALERT: " + e.hazard().name().replace('_', ' ') + " (" + e.severity() + ")"
                + (e.ward() != null ? " - " + e.ward() : "");
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

    /**
     * Decides whether this incident should notify anyone, and why.
     * Returns the reason, or null if it should be SUPPRESSED.
     *  1. The hazard service says its hazard-specific criteria are met.
     *  2. Safety net: severity at or above dpdms.alerts.always-alert-severity.
     */
    private String alertReason(IncidentApprovedEvent e) {
        if (Boolean.TRUE.equals(e.alertCriteriaMet())) {
            return (e.alertReason() != null && !e.alertReason().isBlank())
                    ? e.alertReason().trim()
                    : "Met the " + e.hazard().name().replace('_', ' ') + " alerting criteria";
        }
        Severity always = props.alwaysAlertSeverity();
        if (always != null && e.severity().ordinal() >= always.ordinal()) {
            return "Severity " + e.severity() + " (always alerts at " + always + " or above)";
        }
        return null;
    }

    private String formatMessage(IncidentApprovedEvent e, String reason) {
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
            String summary = e.summary().trim();
            sb.append(" ").append(summary);
            if (!summary.endsWith(".")) {
                sb.append(".");
            }
        }
        if (reason != null) {
            sb.append(" Why: ").append(reason).append(".");
        }
        sb.append(" [Incident #").append(e.incidentId()).append("]");
        String text = sb.toString();
        return text.length() <= 1000 ? text : text.substring(0, 1000);
    }
}
