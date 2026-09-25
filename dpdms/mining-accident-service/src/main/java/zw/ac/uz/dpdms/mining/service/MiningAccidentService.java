package zw.ac.uz.dpdms.mining.service;

import org.springframework.stereotype.Service;
import zw.ac.uz.dpdms.common.AuditAction;
import zw.ac.uz.dpdms.common.Hazard;
import zw.ac.uz.dpdms.common.HazardScopeGuard;
import zw.ac.uz.dpdms.common.IncidentApprovedEvent;
import zw.ac.uz.dpdms.common.IncidentEventPublisher;
import zw.ac.uz.dpdms.common.IncidentStatus;
import zw.ac.uz.dpdms.common.RequestContext;
import zw.ac.uz.dpdms.mining.dto.DecisionRequest;
import zw.ac.uz.dpdms.mining.dto.IncidentCreateRequest;
import zw.ac.uz.dpdms.mining.dto.IncidentResponse;
import zw.ac.uz.dpdms.mining.dto.IncidentUpdateRequest;
import zw.ac.uz.dpdms.mining.entity.MiningAccidentIncident;
import zw.ac.uz.dpdms.mining.entity.MiningAuditLog;
import zw.ac.uz.dpdms.mining.repository.MiningAccidentIncidentRepository;
import zw.ac.uz.dpdms.mining.repository.MiningAuditLogRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class MiningAccidentService {

    // Fixed for this service. When copying this pattern for another
    // hazard service, this is the ONE line that changes to that
    // hazard's own Hazard enum value.
    private static final Hazard SERVICE_HAZARD = Hazard.MINING_ACCIDENT;

    private final MiningAccidentIncidentRepository incidentRepository;
    private final MiningAuditLogRepository auditLogRepository;
    private final HazardScopeGuard scopeGuard;
    private final IncidentEventPublisher eventPublisher;

    public MiningAccidentService(MiningAccidentIncidentRepository incidentRepository,
                                  MiningAuditLogRepository auditLogRepository,
                                  HazardScopeGuard scopeGuard,
                                  IncidentEventPublisher eventPublisher) {
        this.incidentRepository = incidentRepository;
        this.auditLogRepository = auditLogRepository;
        this.scopeGuard = scopeGuard;
        this.eventPublisher = eventPublisher;
    }

    // ---------- CREATE ----------

    public IncidentResponse create(RequestContext ctx, IncidentCreateRequest req) {
        scopeGuard.assertCanCreate(ctx, SERVICE_HAZARD);

        MiningAccidentIncident incident = new MiningAccidentIncident();
        incident.setWard(ctx.ward());               // from the token, never from client input
        incident.setReporterId(ctx.userId());        // from the token, never from client input
        incident.setDistrict(req.district());
        incident.setProvince(req.province());
        incident.setOccurredAt(req.occurredAt());
        incident.setSeverity(req.severity());
        incident.setLatitude(req.latitude());
        incident.setLongitude(req.longitude());
        incident.setStatus(IncidentStatus.PENDING);
        incident.setMineName(req.mineName());
        incident.setMineType(req.mineType());
        incident.setAccidentType(req.accidentType());
        incident.setTrappedOrInjuredCount(req.trappedOrInjuredCount());
        incident.setFatalitiesCount(req.fatalitiesCount());
        incident.setRescueOngoing(req.rescueOngoing());

        incident = incidentRepository.save(incident);
        writeAudit(incident.getId(), AuditAction.SUBMITTED, ctx, "Initial submission");

        return IncidentResponse.from(incident);
    }

    // ---------- READ ----------

    public IncidentResponse getById(RequestContext ctx, Long id) {
        MiningAccidentIncident incident = findOrThrow(id);
        scopeGuard.assertCanView(ctx, SERVICE_HAZARD, incident.getWard(), incident.getReporterId(), incident.getStatus());
        return IncidentResponse.from(incident);
    }

    /**
     * List behaviour depends entirely on the caller's role:
     * - WARD_RECORDER: their own submissions, every status ("my submissions")
     * - PROVINCIAL_SUPERVISOR: this hazard's queue (optionally filtered by status)
     * - PROVINCIAL_ADMIN: everything, any status
     * - NATIONAL_VIEWER: approved records only
     */
    public List<IncidentResponse> list(RequestContext ctx, IncidentStatus statusFilter) {
        if (ctx.isRecorder()) {
            if (ctx.hazard() != SERVICE_HAZARD) {
                throw new zw.ac.uz.dpdms.common.AccessDeniedException(
                        "This account is scoped to " + ctx.hazard() + ", not " + SERVICE_HAZARD);
            }
            return incidentRepository.findByReporterIdOrderByCreatedAtDesc(ctx.userId())
                    .stream().map(IncidentResponse::from).toList();
        }

        if (ctx.isSupervisor()) {
            scopeGuard.assertCanDecide(ctx, SERVICE_HAZARD); // reuses the hazard-match check; supervisors read their own hazard's queue
            List<MiningAccidentIncident> results = (statusFilter != null)
                    ? incidentRepository.findByStatusOrderByCreatedAtDesc(statusFilter)
                    : incidentRepository.findAllByOrderByCreatedAtDesc();
            return results.stream().map(IncidentResponse::from).toList();
        }

        if (ctx.isProvincialAdmin()) {
            List<MiningAccidentIncident> results = (statusFilter != null)
                    ? incidentRepository.findByStatusOrderByCreatedAtDesc(statusFilter)
                    : incidentRepository.findAllByOrderByCreatedAtDesc();
            return results.stream().map(IncidentResponse::from).toList();
        }

        if (ctx.isNationalViewer()) {
            return incidentRepository.findByStatusOrderByCreatedAtDesc(IncidentStatus.APPROVED)
                    .stream().map(IncidentResponse::from).toList();
        }

        throw new zw.ac.uz.dpdms.common.AccessDeniedException("Unrecognised role");
    }

    // ---------- UPDATE (recorder edits their own PENDING or CORRECTIONS_REQUESTED record) ----------

    public IncidentResponse update(RequestContext ctx, Long id, IncidentUpdateRequest req) {
        scopeGuard.assertNotNationalViewer(ctx);
        MiningAccidentIncident incident = findOrThrow(id);
        scopeGuard.assertCanModifyOwnRecord(ctx, SERVICE_HAZARD, incident.getWard(), incident.getReporterId());

        if (incident.getStatus() != IncidentStatus.PENDING && incident.getStatus() != IncidentStatus.CORRECTIONS_REQUESTED) {
            throw new IllegalStateException("Only PENDING or CORRECTIONS_REQUESTED incidents can be edited");
        }

        boolean wasCorrectionsRequested = incident.getStatus() == IncidentStatus.CORRECTIONS_REQUESTED;

        incident.setDistrict(req.district());
        incident.setProvince(req.province());
        incident.setOccurredAt(req.occurredAt());
        incident.setSeverity(req.severity());
        incident.setLatitude(req.latitude());
        incident.setLongitude(req.longitude());
        incident.setMineName(req.mineName());
        incident.setMineType(req.mineType());
        incident.setAccidentType(req.accidentType());
        incident.setTrappedOrInjuredCount(req.trappedOrInjuredCount());
        incident.setFatalitiesCount(req.fatalitiesCount());
        incident.setRescueOngoing(req.rescueOngoing());

        // Editing always resets the record back into the review queue.
        incident.setStatus(IncidentStatus.PENDING);
        incident = incidentRepository.save(incident);

        writeAudit(incident.getId(),
                wasCorrectionsRequested ? AuditAction.RESUBMITTED : AuditAction.SUBMITTED,
                ctx, "Edited by recorder");

        return IncidentResponse.from(incident);
    }

    // ---------- DELETE (recorder deletes their own not-yet-approved record) ----------

    public void delete(RequestContext ctx, Long id) {
        scopeGuard.assertNotNationalViewer(ctx);
        MiningAccidentIncident incident = findOrThrow(id);
        scopeGuard.assertCanModifyOwnRecord(ctx, SERVICE_HAZARD, incident.getWard(), incident.getReporterId());

        if (incident.getStatus() == IncidentStatus.APPROVED) {
            throw new IllegalStateException("Approved incidents cannot be deleted");
        }

        writeAudit(incident.getId(), AuditAction.DELETED, ctx, "Deleted by recorder");
        incidentRepository.delete(incident);
    }

    // ---------- APPROVAL WORKFLOW (supervisor only) ----------

    public IncidentResponse approve(RequestContext ctx, Long id) {
        scopeGuard.assertCanDecide(ctx, SERVICE_HAZARD);
        MiningAccidentIncident incident = findOrThrow(id);
        assertPending(incident);

        incident.setStatus(IncidentStatus.APPROVED);
        incident.setReviewNotes(null);
        incident = incidentRepository.save(incident);
        writeAudit(incident.getId(), AuditAction.APPROVED, ctx, "Approved by supervisor");

        // Tell alert-service (via RabbitMQ). Never throws: if RabbitMQ is
        // down the approval above is still saved and a warning is logged.
        eventPublisher.publishApproved(new IncidentApprovedEvent(
                SERVICE_HAZARD,
                incident.getId(),
                incident.getWard(),
                incident.getDistrict(),
                incident.getProvince(),
                incident.getSeverity(),
                incident.getOccurredAt(),
                alertSummary(incident),
                meetsAlertCriteria(incident),
                alertCriteriaReason(incident)));

        return IncidentResponse.from(incident);
    }

    /**
     * Mining alerting criterion (brief: "a mining accident with
     * casualties") - anyone trapped, injured or killed.
     */
    private boolean meetsAlertCriteria(MiningAccidentIncident incident) {
        return (incident.getFatalitiesCount() != null && incident.getFatalitiesCount() > 0)
                || (incident.getTrappedOrInjuredCount() != null && incident.getTrappedOrInjuredCount() > 0);
    }

    /** Human-readable explanation stored in the alert log either way. */
    private String alertCriteriaReason(MiningAccidentIncident incident) {
        int fatalities = incident.getFatalitiesCount() != null ? incident.getFatalitiesCount() : 0;
        int trapped = incident.getTrappedOrInjuredCount() != null ? incident.getTrappedOrInjuredCount() : 0;
        if (fatalities > 0 || trapped > 0) {
            return fatalities + " fatalities and " + trapped + " trapped/injured - has casualties";
        }
        return "No fatalities or trapped/injured reported";
    }

    /**
     * One-line, hazard-specific summary for the alert message, e.g.
     * "Test Shaft (ARTISANAL), COLLAPSE, 3 trapped/injured, 0 fatalities,
     * rescue ongoing".
     */
    private String alertSummary(MiningAccidentIncident incident) {
        List<String> parts = new ArrayList<>();
        if (incident.getMineName() != null) {
            String mine = incident.getMineName();
            if (incident.getMineType() != null) {
                mine += " (" + incident.getMineType() + ")";
            }
            parts.add(mine);
        }
        if (incident.getAccidentType() != null) {
            parts.add(incident.getAccidentType().name());
        }
        if (incident.getTrappedOrInjuredCount() != null) {
            parts.add(incident.getTrappedOrInjuredCount() + " trapped/injured");
        }
        if (incident.getFatalitiesCount() != null) {
            parts.add(incident.getFatalitiesCount() + " fatalities");
        }
        if (Boolean.TRUE.equals(incident.getRescueOngoing())) {
            parts.add("rescue ongoing");
        }
        return String.join(", ", parts);
    }

    public IncidentResponse reject(RequestContext ctx, Long id, DecisionRequest req) {
        scopeGuard.assertCanDecide(ctx, SERVICE_HAZARD);
        MiningAccidentIncident incident = findOrThrow(id);
        assertPending(incident);

        incident.setStatus(IncidentStatus.REJECTED);
        incident.setReviewNotes(req.notes());
        incident = incidentRepository.save(incident);
        writeAudit(incident.getId(), AuditAction.REJECTED, ctx, req.notes());

        return IncidentResponse.from(incident);
    }

    public IncidentResponse requestCorrections(RequestContext ctx, Long id, DecisionRequest req) {
        scopeGuard.assertCanDecide(ctx, SERVICE_HAZARD);
        MiningAccidentIncident incident = findOrThrow(id);
        assertPending(incident);

        incident.setStatus(IncidentStatus.CORRECTIONS_REQUESTED);
        incident.setReviewNotes(req.notes());
        incident = incidentRepository.save(incident);
        writeAudit(incident.getId(), AuditAction.CORRECTIONS_REQUESTED, ctx, req.notes());

        return IncidentResponse.from(incident);
    }

    // ---------- helpers ----------

    private MiningAccidentIncident findOrThrow(Long id) {
        return incidentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Incident not found: " + id));
    }

    private void assertPending(MiningAccidentIncident incident) {
        if (incident.getStatus() != IncidentStatus.PENDING) {
            throw new IllegalStateException("Only PENDING incidents can be decided on (current status: " + incident.getStatus() + ")");
        }
    }

    private void writeAudit(Long incidentId, AuditAction action, RequestContext ctx, String notes) {
        MiningAuditLog log = new MiningAuditLog();
        log.setIncidentId(incidentId);
        log.setAction(action);
        log.setPerformedByUserId(ctx.userId());
        log.setNotes(notes);
        auditLogRepository.save(log);
    }
}
