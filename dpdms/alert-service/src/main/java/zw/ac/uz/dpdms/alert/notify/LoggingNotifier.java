package zw.ac.uz.dpdms.alert.notify;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import zw.ac.uz.dpdms.alert.entity.DeliveryStatus;

/**
 * Fallback used when NO channel is enabled (the default in development):
 * the alert is written to the application log so the flow can still be
 * tested end to end without email or WhatsApp credentials.
 */
@Component
public class LoggingNotifier {

    private static final Logger log = LoggerFactory.getLogger(LoggingNotifier.class);

    public static final String CHANNEL = "LOG";

    public DeliveryResult write(String messageText) {
        log.info("ALERT (no channel enabled, log only): {}", messageText);
        return new DeliveryResult(DeliveryStatus.LOGGED_ONLY,
                "No alert channel enabled (EMAIL_ENABLED / WHATSAPP_ENABLED) - written to the application log");
    }
}
