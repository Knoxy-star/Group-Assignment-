package zw.ac.uz.dpdms.common;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

/**
 * Reads the X-User-* headers the gateway's JwtAuthenticationFilter
 * forwards, and builds a RequestContext from them. Call this at the
 * top of every controller method that needs to know who's calling.
 *
 * If a header is missing/unparseable, this throws AccessDeniedException
 * (403) rather than silently treating the caller as unauthenticated -
 * a request should never reach a hazard service without having gone
 * through the gateway's JWT check first, so a missing header here
 * means something is misconfigured and should fail loudly, not quietly
 * fall back to an unsafe default.
 */
@Component
public class RequestContextResolver {

    public RequestContext resolve(HttpServletRequest request) {
        String userIdHeader = request.getHeader("X-User-Id");
        String roleHeader = request.getHeader("X-User-Role");
        String hazardHeader = request.getHeader("X-User-Hazard");
        String wardHeader = request.getHeader("X-User-Ward");

        if (userIdHeader == null || userIdHeader.isBlank() || roleHeader == null || roleHeader.isBlank()) {
            throw new AccessDeniedException("Missing authentication context - request did not come through the gateway correctly");
        }

        Long userId;
        Role role;
        try {
            userId = Long.parseLong(userIdHeader);
            role = Role.valueOf(roleHeader);
        } catch (Exception e) {
            throw new AccessDeniedException("Invalid authentication context");
        }

        Hazard hazard = (hazardHeader == null || hazardHeader.isBlank()) ? null : Hazard.valueOf(hazardHeader);
        String ward = (wardHeader == null || wardHeader.isBlank()) ? null : wardHeader;

        return new RequestContext(userId, role, hazard, ward);
    }
}
