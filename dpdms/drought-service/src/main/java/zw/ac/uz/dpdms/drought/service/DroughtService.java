package zw.ac.uz.dpdms.drought.service;

import org.springframework.stereotype.Service;
import zw.ac.uz.dpdms.common.AuditAction;
import zw.ac.uz.dpdms.common.Hazard;
import zw.ac.uz.dpdms.common.HazardScopeGuard;
import zw.ac.uz.dpdms.common.IncidentStatus;
import zw.ac.uz.dpdms.common.RequestContext;
import zw.ac.uz.dpdms.drought.dto.DecisionRequest;
import zw.ac.uz.dpdms.drought.dto.IncidentCreateRequest;
import zw.ac.uz.dpdms.drought.dto.IncidentResponse;
import zw.ac.uz.dpdms.drought.dto.IncidentUpdateRequest;
import zw.ac.uz.dpdms.drought.entity.DroughtIncident;
import zw.ac.uz.dpdms.drought.entity.DroughtAuditLog;
import zw.ac.uz.dpdms.drought.repository.DroughtIncidentRepository;
import zw.ac.uz.dpdms.drought.repository.DroughtAuditLogRepository;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class DroughtService {

    // Fixed for this service. When copying this pattern for another
    // hazard service, this is the ONE line that changes to that
    // hazard's own Hazard enum value.
    private static final Hazard SERVICE_HAZARD = Hazard.DROUGHT;

    private final DroughtIncidentRepository incidentRepository;
    private final DroughtAuditLogRepository auditLogRepository;
    private final HazardScopeGuard scopeGuard;

    public DroughtService(DroughtIncidentRepository incidentRepository,
                                  DroughtAuditLogRepository auditLogRepository,
                                  HazardScopeGuard scopeGuard) {
        this.incidentRepository = incidentRepository;
        this.auditLogRepository = auditLogRepository;
        this.scopeGuard = scopeGuard;
    }

    // ---------- CREATE ----------

    public IncidentResponse create(RequestContext ctx, IncidentCreateRequest req) {
        scopeGuard.assertCanCreate(ctx, SERVICE_HAZARD);

        DroughtIncident incident = new DroughtIncident();
        incident.setWard(ctx.ward());               // from the token, never from client input
        incident.setReporterId(ctx.userId());        // from the token, never from client input
        incident.setDistrict(req.district());
        incident.setProvince(req.province());
        incident.setOccurredAt(req.occurredAt());
        incident.setSeverity(req.severity());
        incident.setLatitude(req.latitude());
        incident.setLongitude(req.longitude());
        incident.setStatus(IncidentStatus.PENDING);
        incident.setRainfallDeficitMm(req.rainfallDeficitMm());
        incident.setConsecutiveDryDays(req.consecutiveDryDays());
        incident.setCropFailurePercent(req.cropFailurePercent());
        incident.setPeopleFacingWaterShortage(req.peopleFacingWaterShortage());
        incident.setLivestockMortalityCount(req.livestockMortalityCount());

        incident = incidentRepository.save(incident);
        writeAudit(incident.getId(), AuditAction.SUBMITTED, ctx, "Initial submission");

        return IncidentResponse.from(incident);
    }

    // ---------- READ ----------

    public IncidentResponse getById(RequestContext ctx, Long id) {
        DroughtIncident incident = findOrThrow(id);
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
            List<DroughtIncident> results = (statusFilter != null)
                    ? incidentRepository.findByStatusOrderByCreatedAtDesc(statusFilter)
                    : incidentRepository.findAllByOrderByCreatedAtDesc();
            return results.stream().map(IncidentResponse::from).toList();
        }

        if (ctx.isProvincialAdmin()) {
            List<DroughtIncident> results = (statusFilter != null)
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
        DroughtIncident incident = findOrThrow(id);
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
        incident.setRainfallDeficitMm(req.rainfallDeficitMm());
        incident.setConsecutiveDryDays(req.consecutiveDryDays());
        incident.setCropFailurePercent(req.cropFailurePercent());
        incident.setPeopleFacingWaterShortage(req.peopleFacingWaterShortage());
        incident.setLivestockMortalityCount(req.livestockMortalityCount());

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
        DroughtIncident incident = findOrThrow(id);
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
        DroughtIncident incident = findOrThrow(id);
        assertPending(incident);

        incident.setStatus(IncidentStatus.APPROVED);
        incident.setReviewNotes(null);
        incident = incidentRepository.save(incident);
        writeAudit(incident.getId(), AuditAction.APPROVED, ctx, "Approved by supervisor");

        return IncidentResponse.from(incident);
    }

    public IncidentResponse reject(RequestContext ctx, Long id, DecisionRequest req) {
        scopeGuard.assertCanDecide(ctx, SERVICE_HAZARD);
        DroughtIncident incident = findOrThrow(id);
        assertPending(incident);

        incident.setStatus(IncidentStatus.REJECTED);
        incident.setReviewNotes(req.notes());
        incident = incidentRepository.save(incident);
        writeAudit(incident.getId(), AuditAction.REJECTED, ctx, req.notes());

        return IncidentResponse.from(incident);
    }

    public IncidentResponse requestCorrections(RequestContext ctx, Long id, DecisionRequest req) {
        scopeGuard.assertCanDecide(ctx, SERVICE_HAZARD);
        DroughtIncident incident = findOrThrow(id);
        assertPending(incident);

        incident.setStatus(IncidentStatus.CORRECTIONS_REQUESTED);
        incident.setReviewNotes(req.notes());
        incident = incidentRepository.save(incident);
        writeAudit(incident.getId(), AuditAction.CORRECTIONS_REQUESTED, ctx, req.notes());

        return IncidentResponse.from(incident);
    }

    // ---------- helpers ----------

    private DroughtIncident findOrThrow(Long id) {
        return incidentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Incident not found: " + id));
    }

    private void assertPending(DroughtIncident incident) {
        if (incident.getStatus() != IncidentStatus.PENDING) {
            throw new IllegalStateException("Only PENDING incidents can be decided on (current status: " + incident.getStatus() + ")");
        }
    }

    private void writeAudit(Long incidentId, AuditAction action, RequestContext ctx, String notes) {
        DroughtAuditLog log = new DroughtAuditLog();
        log.setIncidentId(incidentId);
        log.setAction(action);
        log.setPerformedByUserId(ctx.userId());
        log.setNotes(notes);
        auditLogRepository.save(log);
    }
}
