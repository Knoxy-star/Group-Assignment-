package zw.ac.uz.dpdms.auth.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import zw.ac.uz.dpdms.auth.dto.AuthResponse;
import zw.ac.uz.dpdms.auth.dto.LoginRequest;
import zw.ac.uz.dpdms.auth.dto.RegisterRequest;
import zw.ac.uz.dpdms.auth.entity.Role;
import zw.ac.uz.dpdms.auth.entity.User;
import zw.ac.uz.dpdms.auth.repository.UserRepository;
import zw.ac.uz.dpdms.auth.security.JwtService;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Username already taken: " + request.username());
        }

        validateScoping(request.role(), request.hazard(), request.ward());

        User user = User.builder()
                .username(request.username())
                .passwordHash(passwordEncoder.encode(request.password()))
                .fullName(request.fullName())
                .role(request.role())
                .hazard(request.hazard())
                .ward(request.ward())
                .province(request.province())
                .enabled(true)
                .build();

        userRepository.save(user);
        return toAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));

        if (!user.isEnabled()) {
            throw new IllegalArgumentException("This account has been disabled");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        return toAuthResponse(user);
    }

    /**
     * Enforces at registration time that role/hazard/ward combinations
     * make sense, mirroring the brief's scoping rules:
     * - WARD_RECORDER must have both a hazard and a ward.
     * - PROVINCIAL_SUPERVISOR must have a hazard, must NOT have a ward
     *   (their authority is province-wide within that hazard).
     * - NATIONAL_VIEWER / PROVINCIAL_ADMIN must have neither.
     */
    private void validateScoping(Role role, zw.ac.uz.dpdms.auth.entity.Hazard hazard, String ward) {
        switch (role) {
            case WARD_RECORDER -> {
                if (hazard == null || ward == null || ward.isBlank()) {
                    throw new IllegalArgumentException("WARD_RECORDER requires both a hazard and a ward");
                }
            }
            case PROVINCIAL_SUPERVISOR -> {
                if (hazard == null) {
                    throw new IllegalArgumentException("PROVINCIAL_SUPERVISOR requires a hazard");
                }
                if (ward != null && !ward.isBlank()) {
                    throw new IllegalArgumentException("PROVINCIAL_SUPERVISOR must not have a ward (province-wide role)");
                }
            }
            case NATIONAL_VIEWER, PROVINCIAL_ADMIN -> {
                if (hazard != null || (ward != null && !ward.isBlank())) {
                    throw new IllegalArgumentException(role + " must not have a hazard or ward (cross-hazard role)");
                }
            }
        }
    }

    private AuthResponse toAuthResponse(User user) {
        String token = jwtService.generateToken(user);
        return new AuthResponse(token, user.getId(), user.getUsername(), user.getRole(), user.getHazard(), user.getWard());
    }
}
