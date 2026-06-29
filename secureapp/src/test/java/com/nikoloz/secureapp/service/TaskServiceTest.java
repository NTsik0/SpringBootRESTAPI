package com.nikoloz.secureapp.service;

import com.nikoloz.secureapp.dto.TaskRequest;
import com.nikoloz.secureapp.dto.TaskResponse;
import com.nikoloz.secureapp.exception.ResourceNotFoundException;
import com.nikoloz.secureapp.model.AppUser;
import com.nikoloz.secureapp.model.Task;
import com.nikoloz.secureapp.model.UserRole;
import com.nikoloz.secureapp.repository.TaskRepository;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskService unit tests")
class TaskServiceTest {

    @Mock TaskRepository taskRepository;
    @Mock UserRepository userRepository;

    @InjectMocks TaskService taskService;

    private AppUser owner;
    private Task    task;

    @BeforeEach
    void setUp() {
        owner = new AppUser("nikoloz", "hashed", UserRole.ROLE_USER, "Nikoloz");
        task  = new Task("Write tests", "Add JUnit tests", owner);
    }

    @Test
    @DisplayName("createTask — saves and returns TaskResponse")
    void createTask_success() {
        when(userRepository.findByUsername("nikoloz")).thenReturn(Optional.of(owner));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        TaskResponse result = taskService.createTask(new TaskRequest("Write tests", "Add JUnit tests"), "nikoloz");

        assertThat(result.title()).isEqualTo("Write tests");
        assertThat(result.ownerUsername()).isEqualTo("nikoloz");
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    @DisplayName("createTask — unknown owner throws ResourceNotFoundException")
    void createTask_unknownOwner_throws() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.createTask(new TaskRequest("T", null), "ghost"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("getTasksForUser — returns mapped list")
    void getTasksForUser_returnsList() {
        when(taskRepository.findByOwnerUsername("nikoloz")).thenReturn(List.of(task));

        List<TaskResponse> result = taskService.getTasksForUser("nikoloz");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).title()).isEqualTo("Write tests");
    }

    @Test
    @DisplayName("getTaskById — returns task when owned by user")
    void getTaskById_found() {
        when(taskRepository.findByIdAndOwnerUsername(1L, "nikoloz")).thenReturn(Optional.of(task));

        TaskResponse result = taskService.getTaskById(1L, "nikoloz");

        assertThat(result.title()).isEqualTo("Write tests");
    }

    @ParameterizedTest
    @ValueSource(longs = {99L, 100L, 999L})
    @DisplayName("getTaskById — throws ResourceNotFoundException for various non-existent IDs")
    void getTaskById_notFound_throws(long missingId) {
        when(taskRepository.findByIdAndOwnerUsername(missingId, "nikoloz")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getTaskById(missingId, "nikoloz"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Task not found");
    }

    @Test
    @DisplayName("deleteTask — calls repository delete")
    void deleteTask_success() {
        when(taskRepository.findByIdAndOwnerUsername(1L, "nikoloz")).thenReturn(Optional.of(task));

        taskService.deleteTask(1L, "nikoloz");

        verify(taskRepository).delete(task);
    }

    @ParameterizedTest
    @ValueSource(strings = {"alice", "bob", "stranger"})
    @DisplayName("deleteTask — throws ResourceNotFoundException when task not owned by requesting user")
    void deleteTask_notOwned_throws(String wrongUser) {
        when(taskRepository.findByIdAndOwnerUsername(1L, wrongUser)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.deleteTask(1L, wrongUser))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("toggleComplete — flips completed flag")
    void toggleComplete_flipsFlag() {
        task.setCompleted(false);
        when(taskRepository.findByIdAndOwnerUsername(1L, "nikoloz")).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);

        TaskResponse result = taskService.toggleComplete(1L, "nikoloz");

        assertThat(result.completed()).isTrue();
    }

    @Test
    @DisplayName("updateTask — updates title and description")
    void updateTask_success() {
        when(taskRepository.findByIdAndOwnerUsername(1L, "nikoloz")).thenReturn(Optional.of(task));
        when(taskRepository.save(any())).thenReturn(task);

        TaskResponse result = taskService.updateTask(1L, new TaskRequest("New title", "New desc"), "nikoloz");

        assertThat(result.title()).isEqualTo("New title");
    }
}
