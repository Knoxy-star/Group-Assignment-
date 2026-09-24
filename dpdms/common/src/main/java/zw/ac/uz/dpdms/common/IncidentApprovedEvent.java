package zw.ac.uz.dpdms.common;

import java.time.LocalDateTime;

/**
 * THE CONTRACT between the hazard services and alert-service: the JSON
 * each hazard service publishes when a supervisor approves an incident.
 *
 * Example message body:
 * {
 *   "hazard": "FLOOD",
 *   "incidentId": 1,
 *   "ward": "Rushinga Ward 1",
 *   "district": "Rushinga",
 *   "province": "Mashonaland Central",
 *   "severity": "HIGH",
 *   "occurredAt": "2026-09-23T04:33:00",
 *   "summary": "Mazowe catchment, peak 3.2 m, 45 households displaced",
 *   "alertCriteriaMet": true,
 *   "alertReason": "Peak water level 3.2 m is at or above the 3.0 m danger level"
 * }
 *
 * alertCriteriaMet / alertReason: each hazard service decides, from its
 * OWN indicators, whether the incident meets that hazard's alerting
 * criteria (brief: "a flood above a danger threshold, a fire that is
 * still burning, a zoonotic disease cluster, or a mining accident with
 * casualties"). The hazard service owns that domain knowledge;
 * alert-service only applies the result. null = the service has not
 * implemented its criteria yet (alert-service then only alerts on
 * CRITICAL severity).
 *
 * Lives in common so every hazard service publishes exactly the same
 * shape and alert-service (or any future consumer, e.g. report-service)
 * reads exactly the same shape. Hazard services send it through
 * IncidentEventPublisher; they never build the JSON by hand.
 */
public record IncidentApprovedEvent(
        Hazard hazard,
        Long incidentId,
        String ward,
        String district,
        String province,
        Severity severity,
        LocalDateTime occurredAt,
        String summary,
        Boolean alertCriteriaMet,
        String alertReason
) {

    /**
     * Short form for services that have not added their alerting criteria
     * yet. Keeps older publishApproved(...) calls compiling unchanged.
     */
    public IncidentApprovedEvent(Hazard hazard, Long incidentId, String ward, String district,
                                 String province, Severity severity, LocalDateTime occurredAt,
                                 String summary) {
        this(hazard, incidentId, ward, district, province, severity, occurredAt, summary, null, null);
    }
}
