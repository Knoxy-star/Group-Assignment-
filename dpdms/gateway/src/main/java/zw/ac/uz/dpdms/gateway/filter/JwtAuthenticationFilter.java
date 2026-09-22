package zw.ac.uz.dpdms.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Runs on every request that passes through the gateway (except the
 * whitelisted public paths below). Validates the JWT issued by
 * auth-service, and if it's valid, forwards the caller's role, ward
 * and hazard scope downstream as headers so hazard services don't
 * each have to re-parse the token.
 *
 * IMPORTANT: this is a first line of defence only. Every hazard
 * service MUST independently verify these headers / re-validate
 * authorization server-side (ward+hazard scoping for recorders,
 * hazard scoping for supervisors, read-only for national users).
 * Per the brief, hazard-scoping enforcement that lives only at the
 * gateway is not acceptable - a service must reject a request even
 * if it somehow reached it without going through this filter.
 *
 * The secret is read from jwt.secret (JWT_SECRET env var, same
 * fallback default as auth-service's application.yml). It MUST stay
 * byte-for-byte identical to auth-service's secret - if you change
 * one, change the other, in both places, at the same time.
 */
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    @Value("${jwt.secret:local-dev-only-shared-dpdms-jwt-secret-change-before-submission}")
    private String secret;

    private static final List<String> PUBLIC_PATH_PREFIXES = List.of(
            "/auth-service/api/auth/login",
            "/auth-service/api/auth/register",
            "/eureka"
    );

    // Page shells (/web/**) and static assets are public: the pages
    // themselves carry no data, they just contain JS that calls the
    // real, protected API endpoints (/*/api/**) with a token. The
    // actual RBAC enforcement happens on those API calls, not here.
    private static final List<String> PUBLIC_PATH_CONTAINS = List.of(
            "/web/", "/js/", "/css/", "/webjars/", "/swagger-ui", "/v3/api-docs", "/actuator/health"
    );

    private static final List<String> PUBLIC_PATH_SUFFIXES = List.of(
            ".html", ".js", ".css", ".ico", ".png", ".svg"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        boolean isPublic = PUBLIC_PATH_PREFIXES.stream().anyMatch(path::startsWith)
                || PUBLIC_PATH_CONTAINS.stream().anyMatch(path::contains)
                || PUBLIC_PATH_SUFFIXES.stream().anyMatch(path::endsWith);

        if (isPublic) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange);
        }

        String token = authHeader.substring(7);

        try {
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String role = claims.get("role", String.class);
            String ward = claims.get("ward", String.class);
            String hazard = claims.get("hazard", String.class);
            String userId = claims.getSubject();

            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .header("X-User-Id", userId == null ? "" : userId)
                    .header("X-User-Role", role == null ? "" : role)
                    .header("X-User-Ward", ward == null ? "" : ward)
                    .header("X-User-Hazard", hazard == null ? "" : hazard)
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        } catch (Exception e) {
            return unauthorized(exchange);
        }
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        // Run early, before routing.
        return -1;
    }
}
