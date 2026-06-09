package com.nikoloz.secureapp.config;

import com.nikoloz.secureapp.model.AppUser;
import com.nikoloz.secureapp.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Seeds the in-memory H2 database with test users on startup.
 *
 * Annotated with @Profile("dev") so this class is ONLY instantiated when the
 * application is running under the "dev" profile.  In production the database
 * is managed by migrations (Flyway / Liquibase) and no synthetic data is inserted.
 */
@Slf4j
@Configuration
@Profile("dev")   // ← only active in the dev profile
public class DataInitializer {

    @Bean
    public CommandLineRunner seedDatabase(UserRepository userRepository,
                                          PasswordEncoder passwordEncoder) {
        return args -> {
            log.info("Dev profile active — seeding test data...");

            if (!userRepository.existsByUsername("admin")) {
                userRepository.save(new AppUser(
                        "admin",
                        passwordEncoder.encode("admin123"),
                        "ROLE_ADMIN",
                        "Administrator"
                ));
                log.info("Seeded default admin user — username: 'admin'");
            } else {
                log.debug("Admin user already exists — skipping seed");
            }

            if (!userRepository.existsByUsername("nikoloz")) {
                userRepository.save(new AppUser(
                        "nikoloz",
                        passwordEncoder.encode("user123"),
                        "ROLE_USER",
                        "Nikoloz Tsikaridze"
                ));
                log.info("Seeded default regular user — username: 'nikoloz'");
            } else {
                log.debug("User 'nikoloz' already exists — skipping seed");
            }

            log.debug("Database seed complete — total users in DB: {}", userRepository.count());
        };
    }
}
