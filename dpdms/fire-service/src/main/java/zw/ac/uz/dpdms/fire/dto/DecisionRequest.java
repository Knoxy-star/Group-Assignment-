package zw.ac.uz.dpdms.fire.dto;

import jakarta.validation.constraints.NotBlank;

public record DecisionRequest(
        @NotBlank String notes
) {}
