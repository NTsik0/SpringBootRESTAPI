package com.nikoloz.secureapp.service;

import com.nikoloz.secureapp.model.AppUser;
import com.nikoloz.secureapp.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Here is provided a business logic for user management
 * All logging uses SLF4J parameterised calls no string concatenation,
 * no System.out.println(). Log levels follow the convention:
 *   DEBUG is for internal state useful during local development
 *   INFO is for significant business events (registration, role changes)
 *   WARN is for recoverable problems (user not found, duplicate username)
 *   ERROR is for unexpected failures that need investigation
 */
@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AppUser registerUser(String username, String plainPassword, String displayName) {
        log.debug("Attempting to register new user with username '{}'", username);

        if (userRepository.existsByUsername(username)) {
            log.warn("Registration failed — username '{}' is already taken", username);
            throw new IllegalArgumentException("Username '" + username + "' is already taken.");
        }

        String hashedPassword = passwordEncoder.encode(plainPassword);
        AppUser newUser = new AppUser(username, hashedPassword, "ROLE_USER", displayName);
        AppUser saved = userRepository.save(newUser);

        log.info("New user registered successfully — username: '{}', displayName: '{}'",
                saved.getUsername(), saved.getDisplayName());
        return saved;
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<AppUser> getAllUsers() {
        List<AppUser> users = userRepository.findAll();
        log.debug("Admin fetched user list — total records: {}", users.size());
        return users;
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void disableUser(Long userId) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("disableUser failed — no user found with id {}", userId);
                    return new IllegalArgumentException("User not found with id: " + userId);
                });
        user.setEnabled(false);
        userRepository.save(user);
        log.info("User account disabled — id: {}, username: '{}'", userId, user.getUsername());
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void enableUser(Long userId) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("enableUser failed — no user found with id {}", userId);
                    return new IllegalArgumentException("User not found with id: " + userId);
                });
        user.setEnabled(true);
        userRepository.save(user);
        log.info("User account enabled — id: {}, username: '{}'", userId, user.getUsername());
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(Long userId) {
        log.debug("Admin requesting deletion of user id {}", userId);
        userRepository.deleteById(userId);
        log.info("User deleted — id: {}", userId);
    }

    @PreAuthorize("isAuthenticated()")
    public Optional<AppUser> findByUsername(String username) {
        log.debug("Looking up user by username '{}'", username);
        return userRepository.findByUsername(username);
    }

    public List<AppUser> getUsersByRole(String role) {
        log.debug("Fetching users by role '{}'", role);
        return userRepository.findByRole(role);
    }
}
