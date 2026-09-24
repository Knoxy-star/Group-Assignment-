package zw.ac.uz.dpdms.alert.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/** Bound from the dpdms.whatsapp.* block in application.yml. */
@ConfigurationProperties(prefix = "dpdms.whatsapp")
public record WhatsAppProperties(
        boolean enabled,
        String apiVersion,
        String phoneNumberId,
        String accessToken,
        List<String> recipients,
        String messageType,
        String templateName,
        String templateLanguage
) {
    public List<String> recipientsOrEmpty() {
        return recipients == null ? List.of()
                : recipients.stream().map(String::trim).filter(r -> !r.isEmpty()).toList();
    }
}
