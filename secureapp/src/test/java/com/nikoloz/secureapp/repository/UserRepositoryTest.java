package com.nikoloz.secureapp.repository;

import com.nikoloz.secureapp.model.AppUser;
import com.nikoloz.secureapp.model.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("UserRepository slice tests")
class UserRepositoryTest {

    @Autowired UserRepository userRepository;

    @Test
    @DisplayName("save and findByUsername — persists and retrieves user")
    void saveAndFind() {
        userRepository.save(new AppUser("repouser", "pw", UserRole.ROLE_USER, "Repo User"));

        Optional<AppUser> found = userRepository.findByUsername("repouser");

        assertThat(found).isPresent();
        assertThat(found.get().getDisplayName()).isEqualTo("Repo User");
    }

    @Test
    @DisplayName("existsByUsername — returns true when user exists")
    void existsByUsername_true() {
        userRepository.save(new AppUser("exists", "pw", UserRole.ROLE_USER, "Exists"));

        assertThat(userRepository.existsByUsername("exists")).isTrue();
    }

    @Test
    @DisplayName("existsByUsername — returns false for unknown username")
    void existsByUsername_false() {
        assertThat(userRepository.existsByUsername("nobody")).isFalse();
    }

    @Test
    @DisplayName("findByRole — returns only users with that role")
    void findByRole_correctSubset() {
        userRepository.save(new AppUser("admin1", "pw", UserRole.ROLE_ADMIN, "Admin One"));
        userRepository.save(new AppUser("user1",  "pw", UserRole.ROLE_USER,  "User One"));
        userRepository.save(new AppUser("user2",  "pw", UserRole.ROLE_USER,  "User Two"));

        List<AppUser> admins = userRepository.findByRole(UserRole.ROLE_ADMIN);
        List<AppUser> users  = userRepository.findByRole(UserRole.ROLE_USER);

        assertThat(admins).hasSize(1);
        assertThat(users).hasSize(2);
    }

    @Test
    @DisplayName("findByUsername — returns empty when not found")
    void findByUsername_notFound() {
        Optional<AppUser> result = userRepository.findByUsername("ghost");
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("delete — removes user from database")
    void delete_removesUser() {
        AppUser u = userRepository.save(new AppUser("todelete", "pw", UserRole.ROLE_USER, "Delete Me"));

        userRepository.delete(u);

        assertThat(userRepository.findByUsername("todelete")).isEmpty();
    }
}
