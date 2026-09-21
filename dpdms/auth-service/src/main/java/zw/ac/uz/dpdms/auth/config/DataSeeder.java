package zw.ac.uz.dpdms.auth.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import zw.ac.uz.dpdms.auth.entity.Hazard;
import zw.ac.uz.dpdms.auth.entity.Role;
import zw.ac.uz.dpdms.auth.entity.User;
import zw.ac.uz.dpdms.auth.repository.UserRepository;

/**
 * Seeds a handful of test accounts on first startup so the whole team
 * can log in and test RBAC/scoping immediately without registering
 * users manually every time. Only runs if the users table is empty,
 * so it's safe to leave in across restarts.
 *
 * Password for every seeded account: "Password123!"
 *
 * DELETE OR DISABLE THIS before final submission/demo if you don't
 * want default accounts sitting in the database - for now it's a big
 * time-saver during development.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private static final String SEED_PASSWORD = "Password123!";

    public DataSeeder(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        seedRecorder("flood.recorder", "Flood Ward Recorder", Hazard.FLOOD, "Rushinga Ward 1");
        seedRecorder("drought.recorder", "Drought Ward Recorder", Hazard.DROUGHT, "Rushinga Ward 2");
        seedRecorder("fire.recorder", "Fire Ward Recorder", Hazard.FIRE, "Rushinga Ward 3");
        seedRecorder("zoonotic.recorder", "Zoonotic Disease Ward Recorder", Hazard.ZOONOTIC_DISEASE, "Rushinga Ward 4");
        seedRecorder("mining.recorder", "Mining Accident Ward Recorder", Hazard.MINING_ACCIDENT, "Rushinga Ward 5");

        seedSupervisor("flood.supervisor", "Flood Provincial Supervisor", Hazard.FLOOD);
        seedSupervisor("drought.supervisor", "Drought Provincial Supervisor", Hazard.DROUGHT);
        seedSupervisor("fire.supervisor", "Fire Provincial Supervisor", Hazard.FIRE);
        seedSupervisor("zoonotic.supervisor", "Zoonotic Disease Provincial Supervisor", Hazard.ZOONOTIC_DISEASE);
        seedSupervisor("mining.supervisor", "Mining Accident Provincial Supervisor", Hazard.MINING_ACCIDENT);

        seedCrossHazard("national.viewer", "National Viewer", Role.NATIONAL_VIEWER);
        seedCrossHazard("provincial.admin", "Provincial Administrator", Role.PROVINCIAL_ADMIN);
    }

    private void seedRecorder(String username, String fullName, Hazard hazard, String ward) {
        userRepository.save(User.builder()
                .username(username)
                .passwordHash(passwordEncoder.encode(SEED_PASSWORD))
                .fullName(fullName)
                .role(Role.WARD_RECORDER)
                .hazard(hazard)
                .ward(ward)
                .province("Mashonaland Central")
                .enabled(true)
                .build());
    }

    private void seedSupervisor(String username, String fullName, Hazard hazard) {
        userRepository.save(User.builder()
                .username(username)
                .passwordHash(passwordEncoder.encode(SEED_PASSWORD))
                .fullName(fullName)
                .role(Role.PROVINCIAL_SUPERVISOR)
                .hazard(hazard)
                .province("Mashonaland Central")
                .enabled(true)
                .build());
    }

    private void seedCrossHazard(String username, String fullName, Role role) {
        userRepository.save(User.builder()
                .username(username)
                .passwordHash(passwordEncoder.encode(SEED_PASSWORD))
                .fullName(fullName)
                .role(role)
                .province("Mashonaland Central")
                .enabled(true)
                .build());
    }
}
