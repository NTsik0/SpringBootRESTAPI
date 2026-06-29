package com.nikoloz.secureapp.actuator;

import com.nikoloz.secureapp.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Slf4j
@Component("database")
public class DatabaseHealthIndicator implements HealthIndicator {

    private final UserRepository userRepository;

    public DatabaseHealthIndicator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Health health() {
        try {
            long userCount = userRepository.count();
            log.debug("Health check — user table reachable, count: {}", userCount);
            return Health.up()
                    .withDetail("userCount", userCount)
                    .withDetail("status", "User table reachable")
                    .build();
        } catch (Exception e) {
            log.error("Health check failed — cannot reach user table: {}", e.getMessage());
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
