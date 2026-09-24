package zw.ac.uz.dpdms.alert.notify;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import zw.ac.uz.dpdms.alert.entity.DeliveryStatus;

/** Default notifier (WhatsApp disabled): writes the alert to the log. */
@Component
@ConditionalOnProperty(prefix = "dpdms.whatsapp", name = "enabled", havingValue = "false", matchIfMissing = true)
public class LoggingNotifier implements AlertNotifier {

    private static final Logger log = LoggerFactory.getLogger(LoggingNotifier.class);

    @Override
    public String channelName() {
        return "LOG";
    }

    @Override
    public DeliveryResult send(String messageText) {
        log.info("ALERT (WhatsApp disabled, log only): {}", messageText);
        return new DeliveryResult(DeliveryStatus.LOGGED_ONLY, "WhatsApp disabled - written to log");
    }
}
