package zw.ac.uz.dpdms.mining.dto;

import jakarta.validation.constraints.NotBlank;

public record DecisionRequest(
        @NotBlank String notes
) {}
