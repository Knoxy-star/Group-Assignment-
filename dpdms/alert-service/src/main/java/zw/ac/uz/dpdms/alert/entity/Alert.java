package zw.ac.uz.dpdms.alert.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import zw.ac.uz.dpdms.common.Hazard;
import zw.ac.uz.dpdms.common.Severity;

import java.time.LocalDateTime;

/**
 * One row per approved incident. The unique constraint on
 * (hazard, incident_id) means a redelivered RabbitMQ message can never
 * produce a second alert for the same incident.
 */
@Entity
@Table(name = "alerts",
        uniqueConstraints = @UniqueConstraint(columnNames = {"hazard", "incident_id"}))
@Getter
@Setter
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Hazard hazard;

    @Column(name = "incident_id", nullable = false)
    private Long incidentId;

    private String ward;
    private String district;
    private String province;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity;

    private LocalDateTime occurredAt;

    @Column(length = 500)
    private String summary;

    /** The exact text that was (or would have been) sent. */
    @Column(nullable = false, length = 1000)
    private String message;

    /** Which notifier handled it, e.g. "LOG" or "WHATSAPP". */
    @Column(nullable = false)
    private String channel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryStatus deliveryStatus;

    @Column(length = 1000)
    private String deliveryDetail;

    /** Why this incident did (or did not) trigger a notification. */
    @Column(length = 500)
    private String alertReason;

    @Column(nullable = false, updatable = false)
    private LocalDateTime receivedAt;

    @PrePersist
    void onCreate() {
        receivedAt = LocalDateTime.now();
    }
}
