package zw.ac.uz.dpdms.zoonotic.dto;

import jakarta.validation.constraints.NotBlank;

public record DecisionRequest(
        @NotBlank String notes
) {}
