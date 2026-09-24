package zw.ac.uz.dpdms.alert.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import zw.ac.uz.dpdms.common.Severity;

/**
 * Bound from the dpdms.alerts.* block in application.yml.
 *
 * alwaysAlertSeverity: safety net. An incident at or above this severity
 * always alerts, even if its hazard service says the hazard-specific
 * criteria were not met (or has not implemented them yet).
 */
@ConfigurationProperties(prefix = "dpdms.alerts")
public record AlertProperties(
        String exchange,
        String queue,
        String routingKey,
        Severity alwaysAlertSeverity
) {}
