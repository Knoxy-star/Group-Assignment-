package zw.ac.uz.dpdms.auth.dto;

import zw.ac.uz.dpdms.auth.entity.Hazard;
import zw.ac.uz.dpdms.auth.entity.Role;

public record AuthResponse(
        String token,
        Long userId,
        String username,
        Role role,
        Hazard hazard,
        String ward
) {}
