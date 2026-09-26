package zw.ac.uz.dpdms.mining.service;

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
import zw.ac.uz.dpdms.mining.dto.DecisionRequest;
import zw.ac.uz.dpdms.mining.entity.MiningAccidentIncident;
import zw.ac.uz.dpdms.mining.entity.MiningAccidentIncident.AccidentType;
import zw.ac.uz.dpdms.mining.entity.MiningAccidentIncident.MineType;
import zw.ac.uz.dpdms.mining.repository.MiningAccidentIncidentRepository;
import zw.ac.uz.dpdms.mining.repository.MiningAuditLogRepository;

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
 * Unit tests for the approval workflow and the mining-specific alerting
 * decision (brief: "a mining accident with casualties").
 */
@ExtendWith(MockitoExtension.class)
class MiningAccidentServiceTest {

    @Mock private MiningAccidentIncidentRepository incidentRepository;
    @Mock private MiningAuditLogRepository auditLogRepository;
    @Mock private IncidentEventPublisher eventPublisher;

    private MiningAccidentService service;

    @BeforeEach
    void setUp() {
        service = new MiningAccidentService(incidentRepository, auditLogRepository,
                new HazardScopeGuard(), eventPublisher);
        // lenient: a couple of tests throw before ever reaching save()
        lenient().when(incidentRepository.save(any(MiningAccidentIncident.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private RequestContext supervisor() {
        return new RequestContext(99L, Role.PROVINCIAL_SUPERVISOR, Hazard.MINING_ACCIDENT, null);
    }

    private MiningAccidentIncident pendingIncident(int trapped, int fatalities) {
        MiningAccidentIncident incident = new MiningAccidentIncident();
        incident.setId(1L);
        incident.setWard("Rushinga Ward 5");
        incident.setDistrict("Rushinga");
        incident.setProvince("Mashonaland Central");
        incident.setReporterId(5L);
        incident.setOccurredAt(LocalDateTime.now());
        incident.setSeverity(Severity.HIGH);
        incident.setStatus(IncidentStatus.PENDING);
        incident.setLatitude(-16.71);
        incident.setLongitude(32.18);
        incident.setMineName("Test Shaft");
        incident.setMineType(MineType.ARTISANAL);
        incident.setAccidentType(AccidentType.COLLAPSE);
        incident.setTrappedOrInjuredCount(trapped);
        incident.setFatalitiesCount(fatalities);
        incident.setRescueOngoing(trapped > 0);
        return incident;
    }

    @Test
    void approvingWithNoCasualtiesDoesNotMeetTheAlertCriteria() {
        MiningAccidentIncident incident = pendingIncident(0, 0);
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));

        service.approve(supervisor(), 1L);

        ArgumentCaptor<IncidentApprovedEvent> captor = ArgumentCaptor.forClass(IncidentApprovedEvent.class);
        verify(eventPublisher).publishApproved(captor.capture());
        assertThat(captor.getValue().alertCriteriaMet()).isFalse();
    }

    @Test
    void anyTrappedOrInjuredCountsAsCasualtiesAndMeetsTheAlertCriteria() {
        MiningAccidentIncident incident = pendingIncident(2, 0);
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));

        service.approve(supervisor(), 1L);

        ArgumentCaptor<IncidentApprovedEvent> captor = ArgumentCaptor.forClass(IncidentApprovedEvent.class);
        verify(eventPublisher).publishApproved(captor.capture());
        assertThat(captor.getValue().alertCriteriaMet()).isTrue();
    }

    @Test
    void anyFatalityMeetsTheAlertCriteriaEvenWithZeroTrapped() {
        MiningAccidentIncident incident = pendingIncident(0, 1);
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));

        service.approve(supervisor(), 1L);

        ArgumentCaptor<IncidentApprovedEvent> captor = ArgumentCaptor.forClass(IncidentApprovedEvent.class);
        verify(eventPublisher).publishApproved(captor.capture());
        assertThat(captor.getValue().alertCriteriaMet()).isTrue();
    }

    @Test
    void aFloodSupervisorCannotApproveAMiningAccident() {
        // scopeGuard rejects before the service ever looks the record up,
        // so no repository stubbing is needed here at all.
        RequestContext wrongHazard = new RequestContext(2L, Role.PROVINCIAL_SUPERVISOR, Hazard.FLOOD, null);

        assertThatThrownBy(() -> service.approve(wrongHazard, 1L))
                .isInstanceOf(AccessDeniedException.class);
        verify(eventPublisher, never()).publishApproved(any());
    }

    @Test
    void approvingTwiceIsRejectedTheSecondTime() {
        MiningAccidentIncident incident = pendingIncident(1, 0);
        incident.setStatus(IncidentStatus.APPROVED);
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));

        assertThatThrownBy(() -> service.approve(supervisor(), 1L))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectingStoresTheSupervisorsReasonAndNeverAlerts() {
        MiningAccidentIncident incident = pendingIncident(1, 0);
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));

        var response = service.reject(supervisor(), 1L, new DecisionRequest("Not enough detail"));

        assertThat(response.status()).isEqualTo(IncidentStatus.REJECTED);
        assertThat(response.reviewNotes()).isEqualTo("Not enough detail");
        verify(eventPublisher, never()).publishApproved(any());
    }
}
