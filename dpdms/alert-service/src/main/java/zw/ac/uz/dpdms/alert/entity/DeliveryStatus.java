package zw.ac.uz.dpdms.alert.entity;

/**
 * Used both for the overall alert and for each AlertDelivery row.
 * (Deliberately no new values were added here: MySQL stores this as an
 * ENUM column, which Hibernate's ddl-auto=update does not widen.)
 */
public enum DeliveryStatus {
    /** Delivered to every recipient (overall) / to this recipient (row). */
    SENT,
    /** At least one delivery failed (overall) / this one failed (row) - see detail. */
    FAILED,
    /** No channel enabled; the alert text was written to the application log. */
    LOGGED_ONLY,
    /** Did not meet the alerting criteria - recorded but not sent. */
    SUPPRESSED
}
