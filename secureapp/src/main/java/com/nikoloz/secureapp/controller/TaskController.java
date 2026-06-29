package com.nikoloz.secureapp.controller;

import com.nikoloz.secureapp.dto.TaskRequest;
import com.nikoloz.secureapp.dto.TaskResponse;
import com.nikoloz.secureapp.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Tasks", description = "Task management — create, read, update, delete tasks")
@SecurityRequirement(name = "basicAuth")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    @Operation(summary = "Get all tasks for the authenticated user")
    public ResponseEntity<List<TaskResponse>> getMyTasks(Authentication auth) {
        log.info("GET /api/tasks — user '{}'", auth.getName());
        return ResponseEntity.ok(taskService.getTasksForUser(auth.getName()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single task by ID")
    public ResponseEntity<TaskResponse> getTask(@PathVariable Long id, Authentication auth) {
        log.info("GET /api/tasks/{} — user '{}'", id, auth.getName());
        return ResponseEntity.ok(taskService.getTaskById(id, auth.getName()));
    }

    @PostMapping
    @Operation(summary = "Create a new task")
    public ResponseEntity<TaskResponse> createTask(
            @Valid @RequestBody TaskRequest request,
            Authentication auth) {
        log.info("POST /api/tasks — user '{}', title '{}'", auth.getName(), request.title());
        TaskResponse created = taskService.createTask(request, auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing task")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskRequest request,
            Authentication auth) {
        log.info("PUT /api/tasks/{} — user '{}'", id, auth.getName());
        return ResponseEntity.ok(taskService.updateTask(id, request, auth.getName()));
    }

    @PatchMapping("/{id}/toggle")
    @Operation(summary = "Toggle the completed status of a task")
    public ResponseEntity<TaskResponse> toggleComplete(@PathVariable Long id, Authentication auth) {
        log.info("PATCH /api/tasks/{}/toggle — user '{}'", id, auth.getName());
        return ResponseEntity.ok(taskService.toggleComplete(id, auth.getName()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a task")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id, Authentication auth) {
        log.info("DELETE /api/tasks/{} — user '{}'", id, auth.getName());
        taskService.deleteTask(id, auth.getName());
        return ResponseEntity.noContent().build();
    }
}
