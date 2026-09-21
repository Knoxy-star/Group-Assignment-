package zw.ac.uz.dpdms.auth.entity;

/**
 * The four role types defined in the DPDMS brief.
 *
 * WARD_RECORDER      - scoped to exactly one (ward, hazard) pair. Can
 *                       create/edit records only for their own hazard,
 *                       only in their own ward.
 * PROVINCIAL_SUPERVISOR - scoped to exactly one hazard, province-wide.
 *                       Can approve/reject/request-corrections for that
 *                       hazard only.
 * NATIONAL_VIEWER    - read-only, all hazards, whole country. Every
 *                       write operation must be rejected with 403.
 * PROVINCIAL_ADMIN   - can see pending records across the province
 *                       (per the brief: "the provincial administrator"
 *                       may see a pending record). Kept separate from
 *                       NATIONAL_VIEWER since the brief treats them as
 *                       distinct roles with distinct visibility.
 */
public enum Role {
    WARD_RECORDER,
    PROVINCIAL_SUPERVISOR,
    NATIONAL_VIEWER,
    PROVINCIAL_ADMIN
}
