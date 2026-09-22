package zw.ac.uz.dpdms.mining.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import zw.ac.uz.dpdms.common.Severity;
import zw.ac.uz.dpdms.mining.entity.MiningAccidentIncident.AccidentType;
import zw.ac.uz.dpdms.mining.entity.MiningAccidentIncident.MineType;

import java.time.LocalDateTime;

public record IncidentUpdateRequest(
        @NotBlank String district,
        @NotBlank String province,
        @NotNull LocalDateTime occurredAt,
        @NotNull Severity severity,
        @NotNull Double latitude,
        @NotNull Double longitude,

        @NotBlank String mineName,
        @NotNull MineType mineType,
        @NotNull AccidentType accidentType,
        @NotNull @Min(0) Integer trappedOrInjuredCount,
        @NotNull @Min(0) Integer fatalitiesCount,
        @NotNull Boolean rescueOngoing
) {}
