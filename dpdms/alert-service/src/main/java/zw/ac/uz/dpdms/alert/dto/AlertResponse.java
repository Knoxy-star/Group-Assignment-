package zw.ac.uz.dpdms.alert.dto;

import zw.ac.uz.dpdms.alert.entity.Alert;
import zw.ac.uz.dpdms.alert.entity.DeliveryStatus;
import zw.ac.uz.dpdms.common.Hazard;
import zw.ac.uz.dpdms.common.Severity;

import java.time.LocalDateTime;

public record AlertResponse(
        Long id,
        Hazard hazard,
        Long incidentId,
        String ward,
        String district,
        String province,
        Severity severity,
        LocalDateTime occurredAt,
        String summary,
        String message,
        String channel,
        DeliveryStatus deliveryStatus,
        String deliveryDetail,
        LocalDateTime receivedAt
) {
    public static AlertResponse from(Alert a) {
        return new AlertResponse(
                a.getId(), a.getHazard(), a.getIncidentId(), a.getWard(), a.getDistrict(), a.getProvince(),
                a.getSeverity(), a.getOccurredAt(), a.getSummary(), a.getMessage(), a.getChannel(),
                a.getDeliveryStatus(), a.getDeliveryDetail(), a.getReceivedAt());
    }
}
