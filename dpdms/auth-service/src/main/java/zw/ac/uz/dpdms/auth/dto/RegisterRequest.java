package zw.ac.uz.dpdms.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import zw.ac.uz.dpdms.auth.entity.Hazard;
import zw.ac.uz.dpdms.auth.entity.Role;

public record RegisterRequest(
        @NotBlank String username,
        @NotBlank String password,
        @NotBlank String fullName,
        @NotNull Role role,
        Hazard hazard,   // required for WARD_RECORDER / PROVINCIAL_SUPERVISOR, null otherwise
        String ward,     // required for WARD_RECORDER only
        String province
) {}
