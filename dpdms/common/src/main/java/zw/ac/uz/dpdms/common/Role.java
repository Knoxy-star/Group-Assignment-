package zw.ac.uz.dpdms.common;

/**
 * Must match auth-service's Role enum exactly (name-for-name) since
 * this is what gets deserialized from the X-User-Role header the
 * gateway forwards.
 */
public enum Role {
    WARD_RECORDER,
    PROVINCIAL_SUPERVISOR,
    NATIONAL_VIEWER,
    PROVINCIAL_ADMIN
}
