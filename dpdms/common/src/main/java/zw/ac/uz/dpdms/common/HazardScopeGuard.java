package zw.ac.uz.dpdms.common;

import org.springframework.stereotype.Component;

/**
 *
 * Enforces the brief's access control rules. Every hazard service
 * calls these methods in its service layer (NEVER only in the
 * controller, and NEVER relying on the gateway filter alone) before
 * any read or write. Each hazard service passes its own fixed Hazard
 * value (e.g. Hazard.MINING_ACCIDENT) as `serviceHazard`.
 *
 * Rules implemented here, straight from the brief:
 * - A WARD_RECORDER may write only records for their own (ward, hazard)
 *   pair, and may view/edit only their own submissions.
 * - A PROVINCIAL_SUPERVISOR may approve/reject/request-corrections only
 *   for their own hazard, province-wide (no ward restriction).
 * - A NATIONAL_VIEWER may only read, only APPROVED records, across any
 *   hazard. Every write attempt is rejected.
 * - A PROVINCIAL_ADMIN may view pending records (across the hazard)
 *   but the brief does not grant them write/approval power - only the
 *   hazard-specific supervisor approves. Kept read-only here too,
 *   deliberately more permissive on visibility (can see pending) but
 *   not on mutation.
 */
@Component
public class HazardScopeGuard {

    /** WARD_RECORDER creating a new record for their own hazard/ward. */
    public void assertCanCreate(RequestContext ctx, Hazard serviceHazard) {
        if (!ctx.isRecorder()) {
            throw new AccessDeniedException("Only a ward recorder may submit new incidents");
        }
        assertMatchesServiceHazard(ctx, serviceHazard);
        if (ctx.ward() == null || ctx.ward().isBlank()) {
            throw new AccessDeniedException("Recorder has no ward assigned");
        }
    }

    /** WARD_RECORDER editing/deleting one of their OWN existing records. */
    public void assertCanModifyOwnRecord(RequestContext ctx, Hazard serviceHazard, String recordWard, Long recordReporterId) {
        if (!ctx.isRecorder()) {
            throw new AccessDeniedException("Only the recorder who submitted this incident may modify it");
        }
        assertMatchesServiceHazard(ctx, serviceHazard);
        if (!ctx.ward().equals(recordWard)) {
            throw new AccessDeniedException("Recorder is not authorised for this ward");
        }
        if (!ctx.userId().equals(recordReporterId)) {
            throw new AccessDeniedException("Recorder may only modify their own submissions");
        }
    }

    /** PROVINCIAL_SUPERVISOR approving/rejecting/requesting corrections. */
    public void assertCanDecide(RequestContext ctx, Hazard serviceHazard) {
        if (!ctx.isSupervisor()) {
            throw new AccessDeniedException("Only the provincial supervisor for this hazard may approve, reject or request corrections");
        }
        assertMatchesServiceHazard(ctx, serviceHazard);
    }

    /**
     * Read access to a SINGLE record. Recorder: only their own.
     * Supervisor: any record in their hazard. Provincial admin: any
     * record (they may see pending records too). National viewer:
     * only if the record status is APPROVED.
     */
    public void assertCanView(RequestContext ctx, Hazard serviceHazard, String recordWard, Long recordReporterId, IncidentStatus recordStatus) {
        if (ctx.isNationalViewer()) {
            if (recordStatus != IncidentStatus.APPROVED) {
                throw new AccessDeniedException("National viewers may only view approved incidents");
            }
            return; // national can view any hazard, approved-only
        }
        if (ctx.isProvincialAdmin()) {
            return; // admins may view any record, any status
        }
        if (ctx.isSupervisor()) {
            assertMatchesServiceHazard(ctx, serviceHazard);
            return;
        }
        if (ctx.isRecorder()) {
            assertMatchesServiceHazard(ctx, serviceHazard);
            if (!ctx.ward().equals(recordWard) || !ctx.userId().equals(recordReporterId)) {
                throw new AccessDeniedException("Recorder may only view their own submissions");
            }
            return;
        }
        throw new AccessDeniedException("Unrecognised role");
    }

    /** Every write endpoint (create/update/delete/decide) calls this first as a blanket check. */
    public void assertNotNationalViewer(RequestContext ctx) {
        if (ctx.isNationalViewer()) {
            throw new AccessDeniedException("National viewers are read-only and cannot modify any record");
        }
    }

    private void assertMatchesServiceHazard(RequestContext ctx, Hazard serviceHazard) {
        if (ctx.hazard() != serviceHazard) {
            throw new AccessDeniedException(
                    "This account is scoped to " + ctx.hazard() + " and cannot access " + serviceHazard + " records");
        }
    }
}
