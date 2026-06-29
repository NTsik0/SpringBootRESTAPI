package com.nikoloz.secureapp.repository;

import com.nikoloz.secureapp.model.AppUser;
import com.nikoloz.secureapp.model.Task;
import com.nikoloz.secureapp.model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("TaskRepository slice tests")
class TaskRepositoryTest {

    @Autowired TaskRepository taskRepository;
    @Autowired UserRepository userRepository;

    private AppUser alice;
    private AppUser bob;

    @BeforeEach
    void setUp() {
        alice = userRepository.save(new AppUser("alice", "pw", UserRole.ROLE_USER, "Alice"));
        bob   = userRepository.save(new AppUser("bob",   "pw", UserRole.ROLE_USER, "Bob"));

        taskRepository.save(new Task("Alice task 1", "desc", alice));
        taskRepository.save(new Task("Alice task 2", "desc", alice));
        Task done = new Task("Alice done", "completed task", alice);
        done.setCompleted(true);
        taskRepository.save(done);
        taskRepository.save(new Task("Bob task", "desc", bob));
    }

    @Test
    @DisplayName("findByOwnerUsername — returns only tasks belonging to that user")
    void findByOwnerUsername_returnsCorrectTasks() {
        List<Task> aliceTasks = taskRepository.findByOwnerUsername("alice");
        assertThat(aliceTasks).hasSize(3);
        assertThat(aliceTasks).allMatch(t -> t.getOwner().getUsername().equals("alice"));
    }

    @Test
    @DisplayName("findByOwnerUsername — returns empty list for unknown user")
    void findByOwnerUsername_unknownUser_empty() {
        List<Task> tasks = taskRepository.findByOwnerUsername("nobody");
        assertThat(tasks).isEmpty();
    }

    @Test
    @DisplayName("findByOwnerUsernameAndCompleted — filters by completed flag")
    void findByOwnerUsernameAndCompleted_filtersCorrectly() {
        List<Task> done    = taskRepository.findByOwnerUsernameAndCompleted("alice", true);
        List<Task> pending = taskRepository.findByOwnerUsernameAndCompleted("alice", false);

        assertThat(done).hasSize(1);
        assertThat(pending).hasSize(2);
    }

    @Test
    @DisplayName("findByIdAndOwnerUsername — returns task when owner matches")
    void findByIdAndOwnerUsername_found() {
        Task saved = taskRepository.findByOwnerUsername("bob").get(0);

        Optional<Task> result = taskRepository.findByIdAndOwnerUsername(saved.getId(), "bob");

        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("Bob task");
    }

    @Test
    @DisplayName("findByIdAndOwnerUsername — returns empty when owner does not match")
    void findByIdAndOwnerUsername_wrongOwner_empty() {
        Task aliceTask = taskRepository.findByOwnerUsername("alice").get(0);

        Optional<Task> result = taskRepository.findByIdAndOwnerUsername(aliceTask.getId(), "bob");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("countByOwnerUsername — returns correct count")
    void countByOwnerUsername_correct() {
        assertThat(taskRepository.countByOwnerUsername("alice")).isEqualTo(3);
        assertThat(taskRepository.countByOwnerUsername("bob")).isEqualTo(1);
    }

    @Test
    @DisplayName("save — persists task with correct relationship")
    void save_persistsCorrectly() {
        Task newTask = new Task("New Task", "new desc", alice);
        Task saved = taskRepository.save(newTask);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getOwner().getUsername()).isEqualTo("alice");
        assertThat(saved.isCompleted()).isFalse();
    }
}
