package zw.ac.uz.dpdms.drought.dto;

import jakarta.validation.constraints.NotBlank;

public record DecisionRequest(
        @NotBlank String notes
) {}
