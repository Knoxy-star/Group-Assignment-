package zw.ac.uz.dpdms.common;

/**
 * The approval workflow, identical across all five hazard services.
 *
 *   PENDING --(supervisor approves)--> APPROVED
 *   PENDING --(supervisor rejects)--> REJECTED
 *   PENDING --(supervisor requests corrections)--> CORRECTIONS_REQUESTED
 *   CORRECTIONS_REQUESTED --(recorder edits + resubmits)--> PENDING
 *
 * Only APPROVED records may appear on the dashboard, the map, or in
 * generated reports. A record in any other state is visible only to
 * the recorder who created it, the hazard's provincial supervisor,
 * and provincial admins.
 */
public enum IncidentStatus {
    PENDING,
    APPROVED,
    REJECTED,
    CORRECTIONS_REQUESTED
}
