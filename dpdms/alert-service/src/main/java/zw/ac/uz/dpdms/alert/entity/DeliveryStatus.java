package zw.ac.uz.dpdms.alert.entity;

public enum DeliveryStatus {
    /** Sent to every recipient through the real channel (WhatsApp). */
    SENT,
    /** Real channel attempted but at least one send failed - see deliveryDetail. */
    FAILED,
    /** WhatsApp disabled; the alert text was written to the log instead. */
    LOGGED_ONLY,
    /** Below the configured minimum severity - recorded but not sent. */
    SUPPRESSED
}
