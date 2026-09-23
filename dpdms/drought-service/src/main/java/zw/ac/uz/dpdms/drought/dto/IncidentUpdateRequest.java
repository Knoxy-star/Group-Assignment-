package zw.ac.uz.dpdms.drought.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import zw.ac.uz.dpdms.common.Severity;

import java.time.LocalDateTime;

public record IncidentUpdateRequest(
        // shared fields
        @NotBlank String district,
        @NotBlank String province,
        @NotNull LocalDateTime occurredAt,
        @NotNull Severity severity,
        @NotNull Double latitude,
        @NotNull Double longitude,

        // drought-specific fields
        @NotNull @DecimalMin("0.0") Double rainfallDeficitMm,
        @NotNull @Min(0) Integer consecutiveDryDays,
        @NotNull @DecimalMin("0.0") @DecimalMax("100.0") Double cropFailurePercent,
        @NotNull @Min(0) Integer peopleFacingWaterShortage,
        @NotNull @Min(0) Integer livestockMortalityCount
        // ward and reporterId are NOT here - they come from the caller's
        // RequestContext (the token), never from client input.
) {}
