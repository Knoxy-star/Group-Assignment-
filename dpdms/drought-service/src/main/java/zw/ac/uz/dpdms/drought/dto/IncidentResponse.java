package zw.ac.uz.dpdms.drought.dto;

import zw.ac.uz.dpdms.common.IncidentStatus;
import zw.ac.uz.dpdms.common.Severity;
import zw.ac.uz.dpdms.drought.entity.DroughtIncident;

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

        Double rainfallDeficitMm,
        Integer consecutiveDryDays,
        Double cropFailurePercent,
        Integer peopleFacingWaterShortage,
        Integer livestockMortalityCount
) {
    public static IncidentResponse from(DroughtIncident e) {
        return new IncidentResponse(
                e.getId(), e.getWard(), e.getDistrict(), e.getProvince(),
                e.getOccurredAt(), e.getReporterId(), e.getSeverity(), e.getStatus(),
                e.getLatitude(), e.getLongitude(), e.getReviewNotes(),
                e.getCreatedAt(), e.getUpdatedAt(),
                e.getRainfallDeficitMm(), e.getConsecutiveDryDays(), e.getCropFailurePercent(),
                e.getPeopleFacingWaterShortage(), e.getLivestockMortalityCount()
        );
    }
}
