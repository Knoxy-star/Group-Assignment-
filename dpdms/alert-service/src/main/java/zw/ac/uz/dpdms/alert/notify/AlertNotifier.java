package zw.ac.uz.dpdms.alert.notify;

/**
 * Abstraction over "how an alert reaches people". AlertService only
 * depends on this interface; which implementation is active is decided
 * by configuration (dpdms.whatsapp.enabled). Adding SMS or email later
 * means writing another implementation, not changing AlertService.
 *
 * Implementations must NOT throw on delivery failure - they report it
 * in the DeliveryResult, so one bad phone number can't make RabbitMQ
 * redeliver the whole message.
 */
public interface AlertNotifier {

    /** Short name stored with each alert, e.g. "LOG" or "WHATSAPP". */
    String channelName();

    DeliveryResult send(String messageText);
}
