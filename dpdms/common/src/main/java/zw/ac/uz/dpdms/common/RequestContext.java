package zw.ac.uz.dpdms.common;

/**
 * Represents who is making the current request, as forwarded by the
 * gateway's JwtAuthenticationFilter (X-User-Id, X-User-Role,
 * X-User-Ward, X-User-Hazard headers - derived from the caller's JWT).
 *
 * hazard/ward are null for roles that don't have them (NATIONAL_VIEWER,
 * PROVINCIAL_ADMIN never have a hazard/ward; PROVINCIAL_SUPERVISOR has
 * a hazard but no ward).
 */
public record RequestContext(
        Long userId,
        Role role,
        Hazard hazard,
        String ward
) {
    public boolean isRecorder() {
        return role == Role.WARD_RECORDER;
    }

    public boolean isSupervisor() {
        return role == Role.PROVINCIAL_SUPERVISOR;
    }

    public boolean isNationalViewer() {
        return role == Role.NATIONAL_VIEWER;
    }

    public boolean isProvincialAdmin() {
        return role == Role.PROVINCIAL_ADMIN;
    }
}
