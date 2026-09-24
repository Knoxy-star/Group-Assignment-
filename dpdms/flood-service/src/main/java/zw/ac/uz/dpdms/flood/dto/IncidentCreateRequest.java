package zw.ac.uz.dpdms.flood.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import zw.ac.uz.dpdms.common.Severity;
import zw.ac.uz.dpdms.flood.entity.FloodIncident.Catchment;

import java.time.LocalDateTime;

public record IncidentCreateRequest(
        // shared fields
        @NotBlank String district,
        @NotBlank String province,
        @NotNull LocalDateTime occurredAt,
        @NotNull Severity severity,
        @NotNull Double latitude,
        @NotNull Double longitude,

        // flood-specific fields
        @NotNull @DecimalMin("0.0") Double peakWaterLevelMetres,
        @NotNull Catchment catchment,
        @NotNull @Min(0) Integer householdsDisplaced,
        @NotNull @DecimalMin("0.0") Double areaFloodedHectares,
        @NotNull @Min(0) Integer inundationDurationDays
        // ward and reporterId are NOT here - they come from the
        // authenticated caller's RequestContext, never from client input.
) {}
