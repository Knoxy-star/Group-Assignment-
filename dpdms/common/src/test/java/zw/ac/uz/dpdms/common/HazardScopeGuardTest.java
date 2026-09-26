package zw.ac.uz.dpdms.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for the brief's hazard-scoping rules, in one place since
 * every hazard service (flood, drought, fire, zoonotic-disease,
 * mining-accident) calls this exact same guard - testing it once here
 * covers the scoping logic for all five.
 */
class HazardScopeGuardTest {

    private final HazardScopeGuard guard = new HazardScopeGuard();

    private RequestContext recorder(Hazard hazard, String ward, long userId) {
        return new RequestContext(userId, Role.WARD_RECORDER, hazard, ward);
    }

    private RequestContext supervisor(Hazard hazard) {
        return new RequestContext(99L, Role.PROVINCIAL_SUPERVISOR, hazard, null);
    }

    private RequestContext nationalViewer() {
        return new RequestContext(1L, Role.NATIONAL_VIEWER, null, null);
    }

    private RequestContext provincialAdmin() {
        return new RequestContext(2L, Role.PROVINCIAL_ADMIN, null, null);
    }

    // ---------- assertCanCreate ----------

    @Test
    void recorderCanCreateForOwnWardAndHazard() {
        RequestContext ctx = recorder(Hazard.FLOOD, "Rushinga Ward 1", 1L);
        assertThatCode(() -> guard.assertCanCreate(ctx, Hazard.FLOOD)).doesNotThrowAnyException();
    }

    @Test
    void recorderCannotCreateForADifferentHazard() {
        // A flood recorder calling drought-service's create() - the brief's
        // core rule: a recorder is authorised for exactly one hazard.
        RequestContext ctx = recorder(Hazard.FLOOD, "Rushinga Ward 1", 1L);
        assertThatThrownBy(() -> guard.assertCanCreate(ctx, Hazard.DROUGHT))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void supervisorCannotCreateIncidents() {
        RequestContext ctx = supervisor(Hazard.FLOOD);
        assertThatThrownBy(() -> guard.assertCanCreate(ctx, Hazard.FLOOD))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void recorderWithNoWardCannotCreate() {
        RequestContext ctx = recorder(Hazard.FLOOD, null, 1L);
        assertThatThrownBy(() -> guard.assertCanCreate(ctx, Hazard.FLOOD))
                .isInstanceOf(AccessDeniedException.class);
    }

    // ---------- assertCanModifyOwnRecord ----------

    @Test
    void recorderCanModifyTheirOwnRecordInTheirOwnWard() {
        RequestContext ctx = recorder(Hazard.MINING_ACCIDENT, "Rushinga Ward 5", 5L);
        assertThatCode(() -> guard.assertCanModifyOwnRecord(ctx, Hazard.MINING_ACCIDENT, "Rushinga Ward 5", 5L))
                .doesNotThrowAnyException();
    }

    @Test
    void recorderCannotModifyARecordFromADifferentWard() {
        // Same hazard, same recorder, but the record belongs to another ward
        // - authority is scoped by the (ward, hazard) pair together.
        RequestContext ctx = recorder(Hazard.MINING_ACCIDENT, "Rushinga Ward 5", 5L);
        assertThatThrownBy(() -> guard.assertCanModifyOwnRecord(ctx, Hazard.MINING_ACCIDENT, "Rushinga Ward 9", 5L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void recorderCannotModifySomeoneElsesSubmission() {
        RequestContext ctx = recorder(Hazard.MINING_ACCIDENT, "Rushinga Ward 5", 5L);
        assertThatThrownBy(() -> guard.assertCanModifyOwnRecord(ctx, Hazard.MINING_ACCIDENT, "Rushinga Ward 5", 999L))
                .isInstanceOf(AccessDeniedException.class);
    }

    // ---------- assertCanDecide ----------

    @Test
    void supervisorCanDecideOnlyTheirOwnHazard() {
        RequestContext ctx = supervisor(Hazard.FIRE);
        assertThatCode(() -> guard.assertCanDecide(ctx, Hazard.FIRE)).doesNotThrowAnyException();
        assertThatThrownBy(() -> guard.assertCanDecide(ctx, Hazard.DROUGHT))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void recorderCannotApproveAnything() {
        RequestContext ctx = recorder(Hazard.FIRE, "Rushinga Ward 3", 3L);
        assertThatThrownBy(() -> guard.assertCanDecide(ctx, Hazard.FIRE))
                .isInstanceOf(AccessDeniedException.class);
    }

    // ---------- assertCanView ----------

    @Test
    void nationalViewerCanOnlyViewApprovedRecords() {
        RequestContext ctx = nationalViewer();
        assertThatCode(() -> guard.assertCanView(ctx, Hazard.ZOONOTIC_DISEASE, "any ward", 1L, IncidentStatus.APPROVED))
                .doesNotThrowAnyException();
        assertThatThrownBy(() -> guard.assertCanView(ctx, Hazard.ZOONOTIC_DISEASE, "any ward", 1L, IncidentStatus.PENDING))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void provincialAdminCanViewAnyRecordInAnyStatus() {
        RequestContext ctx = provincialAdmin();
        assertThatCode(() -> guard.assertCanView(ctx, Hazard.DROUGHT, "any ward", 1L, IncidentStatus.PENDING))
                .doesNotThrowAnyException();
    }

    @Test
    void supervisorCanViewAnyWardButOnlyTheirOwnHazard() {
        RequestContext ctx = supervisor(Hazard.DROUGHT);
        assertThatCode(() -> guard.assertCanView(ctx, Hazard.DROUGHT, "Rushinga Ward 2", 1L, IncidentStatus.PENDING))
                .doesNotThrowAnyException();
        assertThatThrownBy(() -> guard.assertCanView(ctx, Hazard.FIRE, "Rushinga Ward 3", 1L, IncidentStatus.PENDING))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void recorderCanOnlyViewTheirOwnSubmission() {
        RequestContext ctx = recorder(Hazard.DROUGHT, "Rushinga Ward 2", 7L);
        assertThatCode(() -> guard.assertCanView(ctx, Hazard.DROUGHT, "Rushinga Ward 2", 7L, IncidentStatus.PENDING))
                .doesNotThrowAnyException();
        assertThatThrownBy(() -> guard.assertCanView(ctx, Hazard.DROUGHT, "Rushinga Ward 2", 999L, IncidentStatus.PENDING))
                .isInstanceOf(AccessDeniedException.class);
    }

    // ---------- assertNotNationalViewer ----------

    @Test
    void nationalViewerIsBlockedFromEveryWriteOperation() {
        assertThatThrownBy(() -> guard.assertNotNationalViewer(nationalViewer()))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void nonNationalViewerRolesPassTheWriteGate() {
        assertThatCode(() -> guard.assertNotNationalViewer(recorder(Hazard.FLOOD, "Rushinga Ward 1", 1L)))
                .doesNotThrowAnyException();
        assertThatCode(() -> guard.assertNotNationalViewer(supervisor(Hazard.FLOOD)))
                .doesNotThrowAnyException();
        assertThatCode(() -> guard.assertNotNationalViewer(provincialAdmin()))
                .doesNotThrowAnyException();
    }

    @Test
    void requestContextRoleHelpersMatchTheRoleTheyWereBuiltWith() {
        assertThat(recorder(Hazard.FLOOD, "W", 1L).isRecorder()).isTrue();
        assertThat(supervisor(Hazard.FLOOD).isSupervisor()).isTrue();
        assertThat(nationalViewer().isNationalViewer()).isTrue();
        assertThat(provincialAdmin().isProvincialAdmin()).isTrue();
    }
}
