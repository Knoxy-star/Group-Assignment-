package zw.ac.uz.dpdms.common;

/**
 * Must match auth-service's Hazard enum exactly. Each hazard service
 * hardcodes which single value from this enum it represents (e.g.
 * mining-accident-service always checks against MINING_ACCIDENT) - see
 * HazardScope in each service's config package.
 */
public enum Hazard {
    FLOOD,
    DROUGHT,
    FIRE,
    ZOONOTIC_DISEASE,
    MINING_ACCIDENT
}
