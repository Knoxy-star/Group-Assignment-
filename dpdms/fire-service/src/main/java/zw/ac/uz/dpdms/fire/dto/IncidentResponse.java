package zw.ac.uz.dpdms.fire.dto;

import zw.ac.uz.dpdms.common.IncidentStatus;
import zw.ac.uz.dpdms.common.Severity;
import zw.ac.uz.dpdms.fire.entity.FireIncident;
import zw.ac.uz.dpdms.fire.entity.FireIncident.SuspectedCause;

import java.time.LocalDateTime;

public record IncidentResponse(
        Long id,
        String ward,
        String district,
        String province,
        LocalDateTime occurredAt,
        Long reporterId,
        Severity severity,
        IncidentStatus status,
        Double latitude,
        Double longitude,
        String reviewNotes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,

        Double areaBurnedHectares,
        SuspectedCause suspectedCause,
        Integer injuriesFatalitiesCount,
        Integer structuresDestroyedCount,
        Boolean contained
) {
    public static IncidentResponse from(FireIncident e) {
        return new IncidentResponse(
                e.getId(), e.getWard(), e.getDistrict(), e.getProvince(),
                e.getOccurredAt(), e.getReporterId(), e.getSeverity(), e.getStatus(),
                e.getLatitude(), e.getLongitude(), e.getReviewNotes(),
                e.getCreatedAt(), e.getUpdatedAt(),
                e.getAreaBurnedHectares(), e.getSuspectedCause(),
                e.getInjuriesFatalitiesCount(), e.getStructuresDestroyedCount(), e.getContained()
        );
    }
}
