package zw.ac.uz.dpdms.zoonotic.dto;

import zw.ac.uz.dpdms.common.IncidentStatus;
import zw.ac.uz.dpdms.common.Severity;
import zw.ac.uz.dpdms.zoonotic.entity.ZoonoticDiseaseIncident;
import zw.ac.uz.dpdms.zoonotic.entity.ZoonoticDiseaseIncident.AnimalSpecies;
import zw.ac.uz.dpdms.zoonotic.entity.ZoonoticDiseaseIncident.OutbreakClassification;

import java.time.LocalDateTime;

public record IncidentResponse(
        Long id,
        String ward,
        String district,
        String province,
        LocalDateTime occurredAt,
        Long reporterId,
        Severity severity,
        IncidentStatus status,
        Double latitude,
        Double longitude,
        String reviewNotes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,

        String diseaseName,
        AnimalSpecies animalSpecies,
        Integer confirmedAnimalCases,
        OutbreakClassification outbreakClassification,
        Integer humanCasesCount
) {
    public static IncidentResponse from(ZoonoticDiseaseIncident e) {
        return new IncidentResponse(
                e.getId(), e.getWard(), e.getDistrict(), e.getProvince(),
                e.getOccurredAt(), e.getReporterId(), e.getSeverity(), e.getStatus(),
                e.getLatitude(), e.getLongitude(), e.getReviewNotes(),
                e.getCreatedAt(), e.getUpdatedAt(),
                e.getDiseaseName(), e.getAnimalSpecies(), e.getConfirmedAnimalCases(),
                e.getOutbreakClassification(), e.getHumanCasesCount()
        );
    }
}
