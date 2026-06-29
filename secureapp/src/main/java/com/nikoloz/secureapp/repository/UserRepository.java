package com.nikoloz.secureapp.repository;

import com.nikoloz.secureapp.model.AppUser;
import com.nikoloz.secureapp.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);
    List<AppUser> findByRole(UserRole role);
    boolean existsByUsername(String username);
}
