package zw.ac.uz.dpdms.alert.notify;

import java.util.List;

/**
 * One delivery channel (email, WhatsApp, ...). AlertService asks every
 * enabled channel to send to each of its recipients and records one
 * AlertDelivery row per attempt. Adding SMS later means writing another
 * implementation; AlertService does not change.
 *
 * send() must NOT throw on delivery failure - it reports the failure in
 * the DeliveryResult, so one bad address can't make RabbitMQ redeliver
 * the whole message (which would re-send to everyone else).
 */
public interface AlertNotifier {

    /** Stored in the delivery log, e.g. "EMAIL" or "WHATSAPP". */
    String channelName();

    /** Switched on via configuration, e.g. EMAIL_ENABLED=true. */
    boolean isEnabled();

    /** Configured recipients for this channel. */
    List<String> recipients();

    /** How the recipient is written to the log (e.g. phone numbers masked). */
    String displayRecipient(String recipient);

    DeliveryResult send(String recipient, String subject, String messageText);
}
