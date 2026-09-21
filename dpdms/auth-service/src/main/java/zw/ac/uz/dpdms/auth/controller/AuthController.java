package zw.ac.uz.dpdms.auth.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import zw.ac.uz.dpdms.auth.dto.AuthResponse;
import zw.ac.uz.dpdms.auth.dto.LoginRequest;
import zw.ac.uz.dpdms.auth.dto.RegisterRequest;
import zw.ac.uz.dpdms.auth.service.AuthService;

import java.util.Map;

/**
 * Reached through the gateway as /auth-service/api/auth/**
 * (the gateway strips the /auth-service prefix before forwarding).
 *
 * Both endpoints here are on the gateway's public-path whitelist
 * (see JwtAuthenticationFilter.PUBLIC_PATHS) since a caller obviously
 * can't have a token before they've logged in.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }
}
