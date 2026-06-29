package com.nikoloz.secureapp.service;

import com.nikoloz.secureapp.exception.ResourceNotFoundException;
import com.nikoloz.secureapp.model.AppUser;
import com.nikoloz.secureapp.model.UserRole;
import com.nikoloz.secureapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService unit tests")
class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;

    @InjectMocks UserService userService;

    private AppUser sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new AppUser("testuser", "hashedpw", UserRole.ROLE_USER, "Test User");
    }

    // registerUser

    @Test
    @DisplayName("registerUser — happy path creates and returns saved user")
    void registerUser_success() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(passwordEncoder.encode("plain123")).thenReturn("hashedpw");
        when(userRepository.save(any(AppUser.class))).thenReturn(sampleUser);

        AppUser result = userService.registerUser("testuser", "plain123", "Test User");

        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getRole()).isEqualTo(UserRole.ROLE_USER);
        verify(userRepository).save(any(AppUser.class));
    }

    @Test
    @DisplayName("registerUser — duplicate username throws IllegalArgumentException")
    void registerUser_duplicateUsername_throws() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        assertThatThrownBy(() -> userService.registerUser("testuser", "pw", "Name"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already taken");

        verify(userRepository, never()).save(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"alice", "bob", "charlie"})
    @DisplayName("registerUser — accepts various valid usernames")
    void registerUser_variousUsernames(String username) {
        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        AppUser saved = new AppUser(username, "hashed", UserRole.ROLE_USER, username);
        when(userRepository.save(any())).thenReturn(saved);

        AppUser result = userService.registerUser(username, "password", username);

        assertThat(result.getUsername()).isEqualTo(username);
    }

    // disableUser / enableUser

    @Test
    @DisplayName("disableUser — sets enabled=false and saves")
    void disableUser_success() {
        sampleUser.setEnabled(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any())).thenReturn(sampleUser);

        userService.disableUser(1L);

        assertThat(sampleUser.isEnabled()).isFalse();
        verify(userRepository).save(sampleUser);
    }

    @Test
    @DisplayName("disableUser — non-existent id throws ResourceNotFoundException")
    void disableUser_notFound_throws() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.disableUser(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("enableUser — sets enabled=true and saves")
    void enableUser_success() {
        sampleUser.setEnabled(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any())).thenReturn(sampleUser);

        userService.enableUser(1L);

        assertThat(sampleUser.isEnabled()).isTrue();
    }

    //  getAllUsers / findByUsername

    @Test
    @DisplayName("getAllUsers — delegates to repository and returns list")
    void getAllUsers_returnsList() {
        when(userRepository.findAll()).thenReturn(List.of(sampleUser));

        List<AppUser> result = userService.getAllUsers();

        assertThat(result).hasSize(1).contains(sampleUser);
    }

    @Test
    @DisplayName("findByUsername — returns Optional when user exists")
    void findByUsername_found() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(sampleUser));

        Optional<AppUser> result = userService.findByUsername("testuser");

        assertThat(result).isPresent().contains(sampleUser);
    }

    @Test
    @DisplayName("findByUsername — returns empty Optional when user not found")
    void findByUsername_notFound() {
        when(userRepository.findByUsername("nobody")).thenReturn(Optional.empty());

        Optional<AppUser> result = userService.findByUsername("nobody");

        assertThat(result).isEmpty();
    }
}
