package zw.ac.uz.dpdms.flood.dto;

import zw.ac.uz.dpdms.common.IncidentStatus;
import zw.ac.uz.dpdms.common.Severity;
import zw.ac.uz.dpdms.flood.entity.FloodIncident;
import zw.ac.uz.dpdms.flood.entity.FloodIncident.Catchment;

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

        Double peakWaterLevelMetres,
        Catchment catchment,
        Integer householdsDisplaced,
        Double areaFloodedHectares,
        Integer inundationDurationDays
) {
    public static IncidentResponse from(FloodIncident e) {
        return new IncidentResponse(
                e.getId(), e.getWard(), e.getDistrict(), e.getProvince(),
                e.getOccurredAt(), e.getReporterId(), e.getSeverity(), e.getStatus(),
                e.getLatitude(), e.getLongitude(), e.getReviewNotes(),
                e.getCreatedAt(), e.getUpdatedAt(),
                e.getPeakWaterLevelMetres(), e.getCatchment(), e.getHouseholdsDisplaced(),
                e.getAreaFloodedHectares(), e.getInundationDurationDays()
        );
    }
}
