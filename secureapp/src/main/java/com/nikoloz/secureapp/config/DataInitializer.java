package com.nikoloz.secureapp.config;

import com.nikoloz.secureapp.model.AppUser;
import com.nikoloz.secureapp.model.Task;
import com.nikoloz.secureapp.model.UserRole;
import com.nikoloz.secureapp.repository.TaskRepository;
import com.nikoloz.secureapp.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Seeds the in-memory H2 database with test users on startup.
 * Annotated with @Profile("dev") so this class is ONLY instantiated when the
 * application is running under the "dev" profile. While it is in production the database is managed by migrations
 * ( Flyway / Liquibase ) and no synthetic data is inserted.
 */
@Slf4j
@Configuration
@Profile("dev")   // ← only active in the dev profile
public class DataInitializer {

    @Bean
    public CommandLineRunner seedDatabase(UserRepository userRepository,
                                          TaskRepository taskRepository,
                                          PasswordEncoder passwordEncoder) {
        return args -> {
            log.info("Dev profile active — seeding test data...");

            AppUser admin;
            if (!userRepository.existsByUsername("admin")) {
                admin = userRepository.save(new AppUser(
                        "admin",
                        passwordEncoder.encode("admin123"),
                        UserRole.ROLE_ADMIN,
                        "Administrator"
                ));
                log.info("Seeded default admin user — username: 'admin'");
            } else {
                admin = userRepository.findByUsername("admin").orElseThrow();
                log.debug("Admin user already exists — skipping seed");
            }

            AppUser nikoloz;
            if (!userRepository.existsByUsername("nikoloz")) {
                nikoloz = userRepository.save(new AppUser(
                        "nikoloz",
                        passwordEncoder.encode("user123"),
                        UserRole.ROLE_USER,
                        "Nikoloz Tsikaridze"
                ));
                log.info("Seeded default regular user — username: 'nikoloz'");
            } else {
                nikoloz = userRepository.findByUsername("nikoloz").orElseThrow();
                log.debug("User 'nikoloz' already exists — skipping seed");
            }

            if (taskRepository.countByOwnerUsername("nikoloz") == 0) {
                taskRepository.save(new Task("Set up development environment", "Install JDK 17, Maven, and IntelliJ IDEA", nikoloz));
                taskRepository.save(new Task("Review Spring Security docs", "Read the official Spring Security reference guide", nikoloz));
                Task done = new Task("Complete Homework 1", "Secure the midterm project with Spring Security", nikoloz);
                done.setCompleted(true);
                taskRepository.save(done);
                log.info("Seeded 3 sample tasks for user 'nikoloz'");
            }

            if (taskRepository.countByOwnerUsername("admin") == 0) {
                taskRepository.save(new Task("Monitor production metrics", "Check /actuator/metrics weekly", admin));
                log.info("Seeded 1 sample task for user 'admin'");
            }

            log.debug("Database seed complete — users: {}, tasks: {}",
                    userRepository.count(), taskRepository.count());
        };
    }
}
