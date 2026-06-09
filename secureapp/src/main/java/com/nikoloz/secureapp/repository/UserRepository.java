package com.nikoloz.secureapp.repository;

import com.nikoloz.secureapp.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);
    List<AppUser> findByRole(String role);
    boolean existsByUsername(String username);
}
