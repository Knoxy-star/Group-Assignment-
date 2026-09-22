package zw.ac.uz.dpdms.mining.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import zw.ac.uz.dpdms.common.BaseIncident;

/**
 * The five mandatory mining-accident indicators, in addition to
 * everything inherited from BaseIncident (ward, district, province,
 * occurredAt, reporterId, severity, status, latitude, longitude,
 * reviewNotes, createdAt, updatedAt).
 *
 * WHEN COPYING THIS PATTERN FOR ANOTHER HAZARD:
 * - keep the class extending BaseIncident
 * - replace the 5 fields below with your hazard's 5 indicators from
 *   the brief (matching types: numbers -> Integer/Double, categorical
 *   -> your own enum, yes/no -> boolean)
 * - change the @Table name to match your service's schema
 */
@Entity
@Table(name = "mining_accident_incidents")
@Getter
@Setter
public class MiningAccidentIncident extends BaseIncident {

    @Column(nullable = false)
    private String mineName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MineType mineType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccidentType accidentType;

    @Column(nullable = false)
    private Integer trappedOrInjuredCount;

    @Column(nullable = false)
    private Integer fatalitiesCount;

    @Column(nullable = false)
    private Boolean rescueOngoing;

    public enum MineType {
        FORMAL,
        ARTISANAL
    }

    public enum AccidentType {
        COLLAPSE,
        GAS_EXPLOSION,
        FLOODING,
        FALL_OF_GROUND
    }
}
