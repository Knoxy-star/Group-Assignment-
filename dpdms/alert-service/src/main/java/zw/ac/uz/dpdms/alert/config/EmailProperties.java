package zw.ac.uz.dpdms.alert.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Bound from the dpdms.email.* block in application.yml. The SMTP login
 * itself (host, username, app password) is Spring Boot's spring.mail.*,
 * also read from environment variables - never typed into the file.
 */
@ConfigurationProperties(prefix = "dpdms.email")
public record EmailProperties(
        boolean enabled,
        String from,
        List<String> recipients
) {
    public List<String> recipientsOrEmpty() {
        return recipients == null ? List.of()
                : recipients.stream().map(String::trim).filter(r -> !r.isEmpty()).toList();
    }
}
