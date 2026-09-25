package zw.ac.uz.dpdms.notification.dto;

import jakarta.validation.constraints.NotBlank;

public record WhatsAppRequest(
        @NotBlank String to,
        @NotBlank String message
) {}

