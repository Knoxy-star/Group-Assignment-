package zw.ac.uz.dpdms.alert.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import zw.ac.uz.dpdms.common.Severity;

/** Bound from the dpdms.alerts.* block in application.yml. */
@ConfigurationProperties(prefix = "dpdms.alerts")
public record AlertProperties(
        String exchange,
        String queue,
        String routingKey,
        Severity minSeverity
) {}
