package zw.ac.uz.dpdms.alert.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * The alert delivery log the brief asks for: "a log of every alert sent,
 * including the channel, recipient, timestamp, and delivery status".
 *
 * One row per (alert, channel, recipient). An alert sent to 2 email
 * addresses and 1 WhatsApp number produces 3 rows, each with its own
 * status, so one failed recipient is visible without hiding the others.
 */
@Entity
@Table(name = "alert_deliveries")
@Getter
@Setter
public class AlertDelivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "alert_id", nullable = false)
    private Alert alert;

    /** EMAIL, WHATSAPP, or LOG (no channel enabled). */
    @Column(nullable = false, length = 20)
    private String channel;

    /** Email address, or WhatsApp number masked to its last 4 digits. */
    @Column(nullable = false)
    private String recipient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DeliveryStatus status;

    /** Provider response or error, e.g. "Accepted by SMTP server". */
    @Column(length = 1000)
    private String detail;

    @Column(nullable = false)
    private LocalDateTime attemptedAt;
}
