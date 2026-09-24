package zw.ac.uz.dpdms.alert.dto;

import zw.ac.uz.dpdms.alert.entity.Alert;
import zw.ac.uz.dpdms.alert.entity.AlertDelivery;
import zw.ac.uz.dpdms.alert.entity.DeliveryStatus;
import zw.ac.uz.dpdms.common.Hazard;
import zw.ac.uz.dpdms.common.Severity;

import java.time.LocalDateTime;
import java.util.List;

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
        String alertReason,
        LocalDateTime receivedAt,
        List<DeliveryResponse> deliveries
) {
    /** One row of the delivery log: channel, recipient, time, status. */
    public record DeliveryResponse(
            String channel,
            String recipient,
            DeliveryStatus status,
            String detail,
            LocalDateTime attemptedAt
    ) {
        static DeliveryResponse from(AlertDelivery d) {
            return new DeliveryResponse(d.getChannel(), d.getRecipient(), d.getStatus(),
                    d.getDetail(), d.getAttemptedAt());
        }
    }

    public static AlertResponse from(Alert a) {
        return new AlertResponse(
                a.getId(), a.getHazard(), a.getIncidentId(), a.getWard(), a.getDistrict(), a.getProvince(),
                a.getSeverity(), a.getOccurredAt(), a.getSummary(), a.getMessage(), a.getChannel(),
                a.getDeliveryStatus(), a.getDeliveryDetail(), a.getAlertReason(), a.getReceivedAt(),
                a.getDeliveries().stream().map(DeliveryResponse::from).toList());
    }
}
