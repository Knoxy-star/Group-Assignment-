package zw.ac.uz.dpdms.auth.entity;

/**
 * The five hazard types. A WARD_RECORDER or PROVINCIAL_SUPERVISOR user
 * is scoped to exactly one of these; NATIONAL_VIEWER and
 * PROVINCIAL_ADMIN users have hazard = null (they see everything, or
 * everything pending, respectively).
 *
 * IMPORTANT for whoever builds a hazard service: the value here must
 * match the value your service checks against the X-User-Hazard header
 * that the gateway forwards. Keep the spelling/casing identical across
 * all services - e.g. always "MINING_ACCIDENT", never "MiningAccident"
 * or "mining_accident" in one service and "MINING_ACCIDENT" in another.
 */
public enum Hazard {
    FLOOD,
    DROUGHT,
    FIRE,
    ZOONOTIC_DISEASE,
    MINING_ACCIDENT
}
