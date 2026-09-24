package zw.ac.uz.dpdms.fire.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import zw.ac.uz.dpdms.common.BaseIncident;

/**
 * The five mandatory fire indicators, in addition to everything
 * inherited from BaseIncident (ward, district, province, occurredAt,
 * reporterId, severity, status, latitude, longitude, reviewNotes,
 * createdAt, updatedAt).
 */
@Entity
@Table(name = "fire_incidents")
@Getter
@Setter
public class FireIncident extends BaseIncident {

    @Column(nullable = false)
    private Double areaBurnedHectares;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SuspectedCause suspectedCause;

    @Column(nullable = false)
    private Integer injuriesFatalitiesCount;

    @Column(nullable = false)
    private Integer structuresDestroyedCount;

    // true = fire is contained, false = still active
    @Column(nullable = false)
    private Boolean contained;

    public enum SuspectedCause {
        NATURAL,
        ACCIDENTAL,
        DELIBERATE
    }
}
