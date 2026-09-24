package zw.ac.uz.dpdms.flood.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import zw.ac.uz.dpdms.common.BaseIncident;

/**
 * The five mandatory flood indicators, in addition to everything
 * inherited from BaseIncident (ward, district, province, occurredAt,
 * reporterId, severity, status, latitude, longitude, reviewNotes,
 * createdAt, updatedAt).
 */
@Entity
@Table(name = "flood_incidents")
@Getter
@Setter
public class FloodIncident extends BaseIncident {

    /** Peak water level reached, in metres. */
    @Column(nullable = false)
    private Double peakWaterLevelMetres;

    /** River basin / catchment the flood occurred in. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Catchment catchment;

    @Column(nullable = false)
    private Integer householdsDisplaced;

    /** Area flooded, in hectares. */
    @Column(nullable = false)
    private Double areaFloodedHectares;

    /** How long the area stayed under water, in days. */
    @Column(nullable = false)
    private Integer inundationDurationDays;

    /**
     * Zimbabwe's seven ZINWA catchment areas. Rushinga falls in the
     * Mazowe catchment, but all seven are listed so the same form
     * works province-wide.
     */
    public enum Catchment {
        GWAYI,
        MANYAME,
        MAZOWE,
        MZINGWANE,
        RUNDE,
        SANYATI,
        SAVE
    }
}
