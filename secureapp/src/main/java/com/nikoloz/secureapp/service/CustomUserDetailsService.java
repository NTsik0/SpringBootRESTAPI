package com.nikoloz.secureapp.service;

import com.nikoloz.secureapp.model.AppUser;
import com.nikoloz.secureapp.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Bridge between our AppUser entity and Spring Security's authentication pipeline.
 * Spring calls loadUserByUsername() on every login attempt.
 * Logging at INFO on success, WARN on failure gives a clear audit trail
 * of authentication events without exposing sensitive data.
 */
@Slf4j
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user details for username '{}'", username);

        AppUser appUser = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Authentication attempt failed — username '{}' not found", username);
                    return new UsernameNotFoundException(
                            "No user found with username: " + username);
                });

        log.info("User '{}' authenticated successfully — role: {}, enabled: {}",
                appUser.getUsername(), appUser.getRole(), appUser.isEnabled());

        return new User(
                appUser.getUsername(),
                appUser.getPassword(),
                appUser.isEnabled(),
                true, true, true,
                List.of(new SimpleGrantedAuthority(appUser.getRole()))
        );
    }
}
