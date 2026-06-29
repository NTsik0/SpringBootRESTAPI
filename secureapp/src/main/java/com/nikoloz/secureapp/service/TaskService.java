package com.nikoloz.secureapp.service;

import com.nikoloz.secureapp.dto.TaskRequest;
import com.nikoloz.secureapp.dto.TaskResponse;
import com.nikoloz.secureapp.exception.ResourceNotFoundException;
import com.nikoloz.secureapp.model.AppUser;
import com.nikoloz.secureapp.model.Task;
import com.nikoloz.secureapp.repository.TaskRepository;
import com.nikoloz.secureapp.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    @PreAuthorize("isAuthenticated()")
    public List<TaskResponse> getTasksForUser(String username) {
        log.debug("Fetching all tasks for user '{}'", username);
        return taskRepository.findByOwnerUsername(username)
                .stream().map(TaskResponse::from).toList();
    }

    @PreAuthorize("isAuthenticated()")
    public TaskResponse getTaskById(Long id, String username) {
        log.debug("Fetching task id {} for user '{}'", id, username);
        Task task = taskRepository.findByIdAndOwnerUsername(id, username)
                .orElseThrow(() -> {
                    log.warn("Task id {} not found or not owned by '{}'", id, username);
                    return new ResourceNotFoundException("Task not found with id: " + id);
                });
        return TaskResponse.from(task);
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional
    public TaskResponse createTask(TaskRequest request, String username) {
        log.debug("Creating task '{}' for user '{}'", request.title(), username);
        AppUser owner = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        Task task = new Task(request.title(), request.description(), owner);
        Task saved = taskRepository.save(task);
        log.info("Task created — id: {}, title: '{}', owner: '{}'", saved.getId(), saved.getTitle(), username);
        return TaskResponse.from(saved);
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional
    public TaskResponse updateTask(Long id, TaskRequest request, String username) {
        log.debug("Updating task id {} for user '{}'", id, username);
        Task task = taskRepository.findByIdAndOwnerUsername(id, username)
                .orElseThrow(() -> {
                    log.warn("Update failed — task id {} not owned by '{}'", id, username);
                    return new ResourceNotFoundException("Task not found with id: " + id);
                });

        task.setTitle(request.title());
        task.setDescription(request.description());
        Task updated = taskRepository.save(task);
        log.info("Task updated — id: {}, new title: '{}'", id, updated.getTitle());
        return TaskResponse.from(updated);
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional
    public TaskResponse toggleComplete(Long id, String username) {
        Task task = taskRepository.findByIdAndOwnerUsername(id, username)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        task.setCompleted(!task.isCompleted());
        Task saved = taskRepository.save(task);
        log.info("Task id {} marked as completed={}", id, saved.isCompleted());
        return TaskResponse.from(saved);
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional
    public void deleteTask(Long id, String username) {
        Task task = taskRepository.findByIdAndOwnerUsername(id, username)
                .orElseThrow(() -> {
                    log.warn("Delete failed — task id {} not owned by '{}'", id, username);
                    return new ResourceNotFoundException("Task not found with id: " + id);
                });
        taskRepository.delete(task);
        log.info("Task deleted — id: {}, owner: '{}'", id, username);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<TaskResponse> getAllTasks() {
        log.debug("Admin fetching all tasks");
        return taskRepository.findAll().stream().map(TaskResponse::from).toList();
    }
}
