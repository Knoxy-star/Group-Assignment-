package zw.ac.uz.dpdms.auth.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import zw.ac.uz.dpdms.auth.entity.User;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

/**
 * Issues the JWTs that every other DPDMS service trusts.
 *
 * IMPORTANT - THE SHARED SECRET:
 * jwt.secret below MUST be byte-for-byte identical to the secret used
 * in gateway's JwtAuthenticationFilter, or tokens issued here will be
 * rejected at the gateway. It is read from the JWT_SECRET environment
 * variable, falling back to a dev-only default if unset so the team
 * can run the system locally without extra setup. Before final
 * submission, set a real JWT_SECRET env var (same value everywhere)
 * and do not rely on the fallback for anything you actually demo.
 */
@Component
public class JwtService {

    @Value("${jwt.secret:local-dev-only-shared-dpdms-jwt-secret-change-before-submission}")
    private String secret;

    @Value("${jwt.expiration-minutes:480}")
    private long expirationMinutes;

    public String generateToken(User user) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        Instant now = Instant.now();

        var builder = Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("username", user.getUsername())
                .claim("role", user.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expirationMinutes * 60)));

        if (user.getHazard() != null) {
            builder.claim("hazard", user.getHazard().name());
        }
        if (user.getWard() != null) {
            builder.claim("ward", user.getWard());
        }

        return builder.signWith(key).compact();
    }
}
