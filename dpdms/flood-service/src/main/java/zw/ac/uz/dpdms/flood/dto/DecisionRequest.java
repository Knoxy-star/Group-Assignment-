package zw.ac.uz.dpdms.flood.dto;

import jakarta.validation.constraints.NotBlank;

public record DecisionRequest(
        @NotBlank String notes
) {}
