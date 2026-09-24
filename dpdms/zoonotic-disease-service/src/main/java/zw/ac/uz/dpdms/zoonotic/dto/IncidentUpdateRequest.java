package zw.ac.uz.dpdms.zoonotic.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import zw.ac.uz.dpdms.common.Severity;
import zw.ac.uz.dpdms.zoonotic.entity.ZoonoticDiseaseIncident.AnimalSpecies;
import zw.ac.uz.dpdms.zoonotic.entity.ZoonoticDiseaseIncident.OutbreakClassification;

import java.time.LocalDateTime;

public record IncidentUpdateRequest(
        @NotBlank String district,
        @NotBlank String province,
        @NotNull LocalDateTime occurredAt,
        @NotNull Severity severity,
        @NotNull Double latitude,
        @NotNull Double longitude,

        @NotBlank String diseaseName,
        @NotNull AnimalSpecies animalSpecies,
        @NotNull @Min(0) Integer confirmedAnimalCases,
        @NotNull OutbreakClassification outbreakClassification,
        @NotNull @Min(0) Integer humanCasesCount
) {}
