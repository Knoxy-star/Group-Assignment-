package zw.ac.uz.dpdms.drought.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import zw.ac.uz.dpdms.common.BaseIncident;

/**
 * The five mandatory drought indicators, in addition to everything
 * inherited from BaseIncident (ward, district, province, occurredAt,
 * reporterId, severity, status, latitude, longitude, reviewNotes,
 * createdAt, updatedAt).
 *
 * All five are numeric, so no hazard-specific enums are needed here
 * (unlike mining's MineType / AccidentType).
 */
@Entity
@Table(name = "drought_incidents")
@Getter
@Setter
public class DroughtIncident extends BaseIncident {

    /** Rainfall shortfall in mm below the seasonal norm for this ward. */
    @Column(nullable = false)
    private Double rainfallDeficitMm;

    /** Number of consecutive days with no meaningful rainfall. */
    @Column(nullable = false)
    private Integer consecutiveDryDays;

    /** Percentage (0-100) of planted crop area that has failed. */
    @Column(nullable = false)
    private Double cropFailurePercent;

    /** Number of people facing water shortages. */
    @Column(nullable = false)
    private Integer peopleFacingWaterShortage;

    /** Number of livestock deaths attributed to the drought. */
    @Column(nullable = false)
    private Integer livestockMortalityCount;
}
