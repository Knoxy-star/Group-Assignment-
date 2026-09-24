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
 *   "summary": "Mazowe catchment, peak 3.2 m, 45 households displaced"
 * }
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
        String summary
) {}
