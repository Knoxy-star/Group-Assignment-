package zw.ac.uz.dpdms.zoonotic.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import zw.ac.uz.dpdms.common.BaseIncident;

/**
 * The five zoonotic-disease indicators, in addition to everything
 * inherited from BaseIncident (ward, district, province, occurredAt,
 * reporterId, severity, status, latitude, longitude, reviewNotes,
 * createdAt, updatedAt).
 */
@Entity
@Table(name = "zoonotic_disease_incidents")
@Getter
@Setter
public class ZoonoticDiseaseIncident extends BaseIncident {

    // 1. Pathogen / disease name (free text, e.g. "Anthrax", "Rabies")
    @Column(nullable = false)
    private String diseaseName;

    // 2. Animal species affected
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AnimalSpecies animalSpecies;

    // 3. Confirmed animal cases
    @Column(nullable = false)
    private Integer confirmedAnimalCases;

    // 4. Cluster vs outbreak classification
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutbreakClassification outbreakClassification;

    // 5. Human cases linked to this event (swap for the brief's 5th indicator if different)
    @Column(nullable = false)
    private Integer humanCasesCount;

    public enum AnimalSpecies {
        CATTLE,
        GOATS,
        SHEEP,
        PIGS,
        POULTRY,
        DOGS,
        WILDLIFE,
        OTHER
    }

    public enum OutbreakClassification {
        CLUSTER,
        OUTBREAK
    }
}
