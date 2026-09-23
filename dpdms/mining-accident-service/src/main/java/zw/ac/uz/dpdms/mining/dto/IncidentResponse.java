package zw.ac.uz.dpdms.mining.dto;

import zw.ac.uz.dpdms.common.IncidentStatus;
import zw.ac.uz.dpdms.common.Severity;
import zw.ac.uz.dpdms.mining.entity.MiningAccidentIncident;
import zw.ac.uz.dpdms.mining.entity.MiningAccidentIncident.AccidentType;
import zw.ac.uz.dpdms.mining.entity.MiningAccidentIncident.MineType;

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

        String mineName,
        MineType mineType,
        AccidentType accidentType,
        Integer trappedOrInjuredCount,
        Integer fatalitiesCount,
        Boolean rescueOngoing
) {
    public static IncidentResponse from(MiningAccidentIncident e) {
        return new IncidentResponse(
                e.getId(), e.getWard(), e.getDistrict(), e.getProvince(),
                e.getOccurredAt(), e.getReporterId(), e.getSeverity(), e.getStatus(),
                e.getLatitude(), e.getLongitude(), e.getReviewNotes(),
                e.getCreatedAt(), e.getUpdatedAt(),
                e.getMineName(), e.getMineType(), e.getAccidentType(),
                e.getTrappedOrInjuredCount(), e.getFatalitiesCount(), e.getRescueOngoing()
        );
    }
}
