package zw.ac.uz.dpdms.flood.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import zw.ac.uz.dpdms.common.AuditAction;
import zw.ac.uz.dpdms.common.Hazard;
import zw.ac.uz.dpdms.common.HazardScopeGuard;
import zw.ac.uz.dpdms.common.IncidentApprovedEvent;
import zw.ac.uz.dpdms.common.IncidentEventPublisher;
import zw.ac.uz.dpdms.common.IncidentStatus;
import zw.ac.uz.dpdms.common.RequestContext;
import zw.ac.uz.dpdms.flood.dto.DecisionRequest;
import zw.ac.uz.dpdms.flood.dto.IncidentCreateRequest;
import zw.ac.uz.dpdms.flood.dto.IncidentResponse;
import zw.ac.uz.dpdms.flood.dto.IncidentUpdateRequest;
import zw.ac.uz.dpdms.flood.entity.FloodIncident;
import zw.ac.uz.dpdms.flood.entity.FloodAuditLog;
import zw.ac.uz.dpdms.flood.repository.FloodIncidentRepository;
import zw.ac.uz.dpdms.flood.repository.FloodAuditLogRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class FloodService {

    // Fixed for this service. When copying this pattern for another
    // hazard service, this is the ONE line that changes to that
    // hazard's own Hazard enum value.
    private static final Hazard SERVICE_HAZARD = Hazard.FLOOD;

    private final FloodIncidentRepository incidentRepository;
    private final FloodAuditLogRepository auditLogRepository;
    private final HazardScopeGuard scopeGuard;
    private final IncidentEventPublisher eventPublisher;

    // Flood alerting criterion (brief: "a flood above a danger threshold").
    // Configurable in application.yml / FLOOD_DANGER_LEVEL_M env var.
    private final double dangerLevelMetres;

    public FloodService(FloodIncidentRepository incidentRepository,
                                  FloodAuditLogRepository auditLogRepository,
                                  HazardScopeGuard scopeGuard,
                                  IncidentEventPublisher eventPublisher,
                                  @Value("${dpdms.alerts.flood.danger-level-m:3.0}") double dangerLevelMetres) {
        this.incidentRepository = incidentRepository;
        this.auditLogRepository = auditLogRepository;
        this.scopeGuard = scopeGuard;
        this.eventPublisher = eventPublisher;
        this.dangerLevelMetres = dangerLevelMetres;
    }

    // ---------- CREATE ----------

    public IncidentResponse create(RequestContext ctx, IncidentCreateRequest req) {
        scopeGuard.assertCanCreate(ctx, SERVICE_HAZARD);

        FloodIncident incident = new FloodIncident();
        incident.setWard(ctx.ward());               // from the token, never from client input
        incident.setReporterId(ctx.userId());        // from the token, never from client input
        incident.setDistrict(req.district());
        incident.setProvince(req.province());
        incident.setOccurredAt(req.occurredAt());
        incident.setSeverity(req.severity());
        incident.setLatitude(req.latitude());
        incident.setLongitude(req.longitude());
        incident.setStatus(IncidentStatus.PENDING);
        incident.setPeakWaterLevelMetres(req.peakWaterLevelMetres());
        incident.setCatchment(req.catchment());
        incident.setHouseholdsDisplaced(req.householdsDisplaced());
        incident.setAreaFloodedHectares(req.areaFloodedHectares());
        incident.setInundationDurationDays(req.inundationDurationDays());

        incident = incidentRepository.save(incident);
        writeAudit(incident.getId(), AuditAction.SUBMITTED, ctx, "Initial submission");

        return IncidentResponse.from(incident);
    }

    // ---------- READ ----------

    public IncidentResponse getById(RequestContext ctx, Long id) {
        FloodIncident incident = findOrThrow(id);
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
            List<FloodIncident> results = (statusFilter != null)
                    ? incidentRepository.findByStatusOrderByCreatedAtDesc(statusFilter)
                    : incidentRepository.findAllByOrderByCreatedAtDesc();
            return results.stream().map(IncidentResponse::from).toList();
        }

        if (ctx.isProvincialAdmin()) {
            List<FloodIncident> results = (statusFilter != null)
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
        FloodIncident incident = findOrThrow(id);
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
        incident.setPeakWaterLevelMetres(req.peakWaterLevelMetres());
        incident.setCatchment(req.catchment());
        incident.setHouseholdsDisplaced(req.householdsDisplaced());
        incident.setAreaFloodedHectares(req.areaFloodedHectares());
        incident.setInundationDurationDays(req.inundationDurationDays());

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
        FloodIncident incident = findOrThrow(id);
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
        FloodIncident incident = findOrThrow(id);
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
     * Flood alerting criterion: peak water level at or above the danger
     * level. Kept here, in flood-service, because only this service
     * understands flood indicators; alert-service just acts on the result.
     */
    private boolean meetsAlertCriteria(FloodIncident incident) {
        Double peak = incident.getPeakWaterLevelMetres();
        return peak != null && peak >= dangerLevelMetres;
    }

    /** Human-readable explanation stored in the alert log either way. */
    private String alertCriteriaReason(FloodIncident incident) {
        Double peak = incident.getPeakWaterLevelMetres();
        if (peak == null) {
            return "No peak water level recorded";
        }
        return "Peak water level " + peak + " m is "
                + (peak >= dangerLevelMetres ? "at or above" : "below")
                + " the " + dangerLevelMetres + " m danger level";
    }

    /**
     * One-line, hazard-specific summary for the alert message, built from
     * the flood indicators, e.g. "Mazowe catchment, peak 3.2 m, 45
     * households displaced, 120.0 ha flooded for 3 days". Skips any
     * indicator that is missing.
     */
    private String alertSummary(FloodIncident incident) {
        List<String> parts = new ArrayList<>();
        if (incident.getCatchment() != null) {
            String name = incident.getCatchment().name();
            parts.add(name.charAt(0) + name.substring(1).toLowerCase() + " catchment");
        }
        if (incident.getPeakWaterLevelMetres() != null) {
            parts.add("peak " + incident.getPeakWaterLevelMetres() + " m");
        }
        if (incident.getHouseholdsDisplaced() != null) {
            parts.add(incident.getHouseholdsDisplaced() + " households displaced");
        }
        if (incident.getAreaFloodedHectares() != null) {
            String area = incident.getAreaFloodedHectares() + " ha flooded";
            if (incident.getInundationDurationDays() != null) {
                area += " for " + incident.getInundationDurationDays() + " days";
            }
            parts.add(area);
        }
        return String.join(", ", parts);
    }

    public IncidentResponse reject(RequestContext ctx, Long id, DecisionRequest req) {
        scopeGuard.assertCanDecide(ctx, SERVICE_HAZARD);
        FloodIncident incident = findOrThrow(id);
        assertPending(incident);

        incident.setStatus(IncidentStatus.REJECTED);
        incident.setReviewNotes(req.notes());
        incident = incidentRepository.save(incident);
        writeAudit(incident.getId(), AuditAction.REJECTED, ctx, req.notes());

        return IncidentResponse.from(incident);
    }

    public IncidentResponse requestCorrections(RequestContext ctx, Long id, DecisionRequest req) {
        scopeGuard.assertCanDecide(ctx, SERVICE_HAZARD);
        FloodIncident incident = findOrThrow(id);
        assertPending(incident);

        incident.setStatus(IncidentStatus.CORRECTIONS_REQUESTED);
        incident.setReviewNotes(req.notes());
        incident = incidentRepository.save(incident);
        writeAudit(incident.getId(), AuditAction.CORRECTIONS_REQUESTED, ctx, req.notes());

        return IncidentResponse.from(incident);
    }

    // ---------- helpers ----------

    private FloodIncident findOrThrow(Long id) {
        return incidentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Incident not found: " + id));
    }

    private void assertPending(FloodIncident incident) {
        if (incident.getStatus() != IncidentStatus.PENDING) {
            throw new IllegalStateException("Only PENDING incidents can be decided on (current status: " + incident.getStatus() + ")");
        }
    }

    private void writeAudit(Long incidentId, AuditAction action, RequestContext ctx, String notes) {
        FloodAuditLog log = new FloodAuditLog();
        log.setIncidentId(incidentId);
        log.setAction(action);
        log.setPerformedByUserId(ctx.userId());
        log.setNotes(notes);
        auditLogRepository.save(log);
    }
}
