package zw.ac.uz.dpdms.flood.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import zw.ac.uz.dpdms.common.AccessDeniedException;
import zw.ac.uz.dpdms.common.Hazard;
import zw.ac.uz.dpdms.common.HazardScopeGuard;
import zw.ac.uz.dpdms.common.IncidentApprovedEvent;
import zw.ac.uz.dpdms.common.IncidentEventPublisher;
import zw.ac.uz.dpdms.common.IncidentStatus;
import zw.ac.uz.dpdms.common.RequestContext;
import zw.ac.uz.dpdms.common.Role;
import zw.ac.uz.dpdms.common.Severity;
import zw.ac.uz.dpdms.flood.dto.DecisionRequest;
import zw.ac.uz.dpdms.flood.dto.IncidentCreateRequest;
import zw.ac.uz.dpdms.flood.entity.FloodAuditLog;
import zw.ac.uz.dpdms.flood.entity.FloodIncident;
import zw.ac.uz.dpdms.flood.entity.FloodIncident.Catchment;
import zw.ac.uz.dpdms.flood.repository.FloodAuditLogRepository;
import zw.ac.uz.dpdms.flood.repository.FloodIncidentRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the approval workflow (PENDING -> APPROVED / REJECTED /
 * CORRECTIONS_REQUESTED) and the flood-specific alerting decision.
 * Repositories and the event publisher are mocked; HazardScopeGuard is
 * real, since exercising the actual scoping rules together with the
 * workflow is the point.
 */
@ExtendWith(MockitoExtension.class)
class FloodServiceTest {

    private static final double DANGER_LEVEL_M = 3.0;

    @Mock private FloodIncidentRepository incidentRepository;
    @Mock private FloodAuditLogRepository auditLogRepository;
    @Mock private IncidentEventPublisher eventPublisher;

    private FloodService service;

    @BeforeEach
    void setUp() {
        service = new FloodService(incidentRepository, auditLogRepository,
                new HazardScopeGuard(), eventPublisher, DANGER_LEVEL_M);
        // lenient: a couple of tests throw before ever reaching save()
        lenient().when(incidentRepository.save(any(FloodIncident.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private RequestContext recorder() {
        return new RequestContext(1L, Role.WARD_RECORDER, Hazard.FLOOD, "Rushinga Ward 1");
    }

    private RequestContext supervisor() {
        return new RequestContext(99L, Role.PROVINCIAL_SUPERVISOR, Hazard.FLOOD, null);
    }

    private IncidentCreateRequest createRequest(double peakWaterLevel) {
        return new IncidentCreateRequest("Rushinga", "Mashonaland Central", LocalDateTime.now(),
                Severity.HIGH, -16.7, 32.15, peakWaterLevel, Catchment.MAZOWE, 50, 20.0, 2);
    }

    private FloodIncident pendingIncident(double peakWaterLevel) {
        FloodIncident incident = new FloodIncident();
        incident.setId(1L);
        incident.setWard("Rushinga Ward 1");
        incident.setDistrict("Rushinga");
        incident.setProvince("Mashonaland Central");
        incident.setReporterId(1L);
        incident.setOccurredAt(LocalDateTime.now());
        incident.setSeverity(Severity.HIGH);
        incident.setStatus(IncidentStatus.PENDING);
        incident.setLatitude(-16.7);
        incident.setLongitude(32.15);
        incident.setPeakWaterLevelMetres(peakWaterLevel);
        incident.setCatchment(Catchment.MAZOWE);
        incident.setHouseholdsDisplaced(50);
        incident.setAreaFloodedHectares(20.0);
        incident.setInundationDurationDays(2);
        return incident;
    }

    // ---------- create ----------

    @Test
    void createTakesWardAndReporterFromTheTokenNotTheRequestBody() {
        var response = service.create(recorder(), createRequest(1.0));

        assertThat(response.ward()).isEqualTo("Rushinga Ward 1");
        assertThat(response.status()).isEqualTo(IncidentStatus.PENDING);
        verify(auditLogRepository).save(any(FloodAuditLog.class));
    }

    @Test
    void aDroughtSupervisorCannotSubmitAFloodIncident() {
        RequestContext wrongHazard = new RequestContext(1L, Role.WARD_RECORDER, Hazard.DROUGHT, "Rushinga Ward 2");
        assertThatThrownBy(() -> service.create(wrongHazard, createRequest(1.0)))
                .isInstanceOf(AccessDeniedException.class);
    }

    // ---------- approve ----------

    @Test
    void approvingAPendingIncidentSetsApprovedAndPublishesTheEvent() {
        FloodIncident incident = pendingIncident(1.0); // below the danger level
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));

        var response = service.approve(supervisor(), 1L);

        assertThat(response.status()).isEqualTo(IncidentStatus.APPROVED);
        verify(eventPublisher).publishApproved(any(IncidentApprovedEvent.class));
    }

    @Test
    void approvingAnAlreadyDecidedIncidentIsRejected() {
        FloodIncident incident = pendingIncident(1.0);
        incident.setStatus(IncidentStatus.APPROVED);
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));

        assertThatThrownBy(() -> service.approve(supervisor(), 1L))
                .isInstanceOf(IllegalStateException.class);
        verify(eventPublisher, never()).publishApproved(any());
    }

    @Test
    void aFireSupervisorCannotApproveAFloodIncident() {
        // scopeGuard rejects before the service ever looks the record up,
        // so no repository stubbing is needed here at all.
        RequestContext wrongHazard = new RequestContext(2L, Role.PROVINCIAL_SUPERVISOR, Hazard.FIRE, null);

        assertThatThrownBy(() -> service.approve(wrongHazard, 1L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void peakWaterLevelAtOrAboveTheDangerLevelMeetsTheAlertCriteria() {
        FloodIncident incident = pendingIncident(4.5); // above the 3.0 m danger level
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));

        service.approve(supervisor(), 1L);

        ArgumentCaptor<IncidentApprovedEvent> captor = ArgumentCaptor.forClass(IncidentApprovedEvent.class);
        verify(eventPublisher).publishApproved(captor.capture());
        assertThat(captor.getValue().alertCriteriaMet()).isTrue();
    }

    @Test
    void peakWaterLevelBelowTheDangerLevelDoesNotMeetTheAlertCriteria() {
        FloodIncident incident = pendingIncident(1.0); // below the 3.0 m danger level
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));

        service.approve(supervisor(), 1L);

        ArgumentCaptor<IncidentApprovedEvent> captor = ArgumentCaptor.forClass(IncidentApprovedEvent.class);
        verify(eventPublisher).publishApproved(captor.capture());
        assertThat(captor.getValue().alertCriteriaMet()).isFalse();
    }

    // ---------- reject / request corrections ----------

    @Test
    void rejectingRecordsTheSupervisorsReasonAndNeverPublishesAnAlert() {
        FloodIncident incident = pendingIncident(1.0);
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));

        var response = service.reject(supervisor(), 1L, new DecisionRequest("Coordinates look wrong"));

        assertThat(response.status()).isEqualTo(IncidentStatus.REJECTED);
        assertThat(response.reviewNotes()).isEqualTo("Coordinates look wrong");
        verify(eventPublisher, never()).publishApproved(any());
    }

    @Test
    void requestingCorrectionsMovesItBackToTheRecorderWithNotes() {
        FloodIncident incident = pendingIncident(1.0);
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));

        var response = service.requestCorrections(supervisor(), 1L, new DecisionRequest("Please add households displaced"));

        assertThat(response.status()).isEqualTo(IncidentStatus.CORRECTIONS_REQUESTED);
        assertThat(response.reviewNotes()).isEqualTo("Please add households displaced");
    }

    // ---------- resubmission after corrections ----------

    @Test
    void editingACorrectionsRequestedRecordResubmitsItAsPending() {
        FloodIncident incident = pendingIncident(1.0);
        incident.setStatus(IncidentStatus.CORRECTIONS_REQUESTED);
        incident.setReviewNotes("Please add households displaced");
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));

        var response = service.update(recorder(), 1L, new zw.ac.uz.dpdms.flood.dto.IncidentUpdateRequest(
                "Rushinga", "Mashonaland Central", LocalDateTime.now(), Severity.HIGH, -16.7, 32.15,
                1.0, Catchment.MAZOWE, 60, 20.0, 2));

        assertThat(response.status()).isEqualTo(IncidentStatus.PENDING);
    }

    // ---------- delete ----------

    @Test
    void anApprovedRecordCanNoLongerBeDeleted() {
        FloodIncident incident = pendingIncident(1.0);
        incident.setStatus(IncidentStatus.APPROVED);
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));

        assertThatThrownBy(() -> service.delete(recorder(), 1L))
                .isInstanceOf(IllegalStateException.class);
    }
}
