package com.nikoloz.secureapp.repository;

import com.nikoloz.secureapp.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByOwnerUsername(String username);
    List<Task> findByOwnerUsernameAndCompleted(String username, boolean completed);
    Optional<Task> findByIdAndOwnerUsername(Long id, String username);
    long countByOwnerUsername(String username);
}
