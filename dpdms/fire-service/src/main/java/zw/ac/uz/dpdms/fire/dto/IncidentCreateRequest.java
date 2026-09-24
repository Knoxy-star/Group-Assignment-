package zw.ac.uz.dpdms.fire.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import zw.ac.uz.dpdms.common.Severity;
import zw.ac.uz.dpdms.fire.entity.FireIncident.SuspectedCause;

import java.time.LocalDateTime;

public record IncidentCreateRequest(
        // shared fields
        @NotBlank String district,
        @NotBlank String province,
        @NotNull LocalDateTime occurredAt,
        @NotNull Severity severity,
        @NotNull Double latitude,
        @NotNull Double longitude,

        // fire-specific fields
        @NotNull @Min(0) Double areaBurnedHectares,
        @NotNull SuspectedCause suspectedCause,
        @NotNull @Min(0) Integer injuriesFatalitiesCount,
        @NotNull @Min(0) Integer structuresDestroyedCount,
        @NotNull Boolean contained
        // Note: ward and reporterId are NOT here - they come from the
        // authenticated caller's RequestContext, never from client
        // input, so a recorder can't spoof a different ward.
) {}
