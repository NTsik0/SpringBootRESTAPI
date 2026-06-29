package com.nikoloz.secureapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nikoloz.secureapp.config.SecurityConfig;
import com.nikoloz.secureapp.dto.TaskRequest;
import com.nikoloz.secureapp.dto.TaskResponse;
import com.nikoloz.secureapp.exception.ResourceNotFoundException;
import com.nikoloz.secureapp.service.CustomUserDetailsService;
import com.nikoloz.secureapp.service.TaskService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
@Import(SecurityConfig.class)
@DisplayName("TaskController — @WebMvcTest")
class TaskControllerTest {

    @Autowired MockMvc       mockMvc;
    @Autowired ObjectMapper  objectMapper;

    @MockBean TaskService              taskService;
    @MockBean CustomUserDetailsService customUserDetailsService;

    private final TaskResponse sampleResponse =
            new TaskResponse(1L, "Write tests", "JUnit tests", false, "nikoloz");

    // GET /api/tasks

    @Test
    @WithMockUser(username = "nikoloz")
    @DisplayName("GET /api/tasks — authenticated user gets their tasks")
    void getMyTasks_authenticated_returns200() throws Exception {
        when(taskService.getTasksForUser("nikoloz")).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Write tests"))
                .andExpect(jsonPath("$[0].ownerUsername").value("nikoloz"));
    }

    @Test
    @DisplayName("GET /api/tasks — unauthenticated returns 401")
    void getMyTasks_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isUnauthorized());
    }

    //  GET /api/tasks/{id}

    @Test
    @WithMockUser(username = "nikoloz")
    @DisplayName("GET /api/tasks/{id} — returns task for owner")
    void getTask_found_returns200() throws Exception {
        when(taskService.getTaskById(1L, "nikoloz")).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(username = "nikoloz")
    @DisplayName("GET /api/tasks/{id} — not found returns 404")
    void getTask_notFound_returns404() throws Exception {
        when(taskService.getTaskById(99L, "nikoloz"))
                .thenThrow(new ResourceNotFoundException("Task not found with id: 99"));

        mockMvc.perform(get("/api/tasks/99"))
                .andExpect(status().isNotFound());
    }

    //POST /api/tasks

    @Test
    @WithMockUser(username = "nikoloz")
    @DisplayName("POST /api/tasks — valid request creates task")
    void createTask_valid_returns201() throws Exception {
        TaskRequest req = new TaskRequest("Write tests", "JUnit tests");
        when(taskService.createTask(any(TaskRequest.class), eq("nikoloz"))).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Write tests"));
    }

    @Test
    @WithMockUser(username = "nikoloz")
    @DisplayName("POST /api/tasks — blank title returns 400")
    void createTask_blankTitle_returns400() throws Exception {
        TaskRequest req = new TaskRequest("", "some desc");

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());

        verify(taskService, never()).createTask(any(), any());
    }

    //  PUT /api/tasks/{id}

    @Test
    @WithMockUser(username = "nikoloz")
    @DisplayName("PUT /api/tasks/{id} — valid update returns 200")
    void updateTask_valid_returns200() throws Exception {
        TaskRequest req = new TaskRequest("Updated title", "Updated desc");
        TaskResponse updated = new TaskResponse(1L, "Updated title", "Updated desc", false, "nikoloz");
        when(taskService.updateTask(eq(1L), any(TaskRequest.class), eq("nikoloz"))).thenReturn(updated);

        mockMvc.perform(put("/api/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated title"));
    }

    //  PATCH /api/tasks/{id}/toggle

    @Test
    @WithMockUser(username = "nikoloz")
    @DisplayName("PATCH /api/tasks/{id}/toggle — toggles completed flag")
    void toggleComplete_returns200() throws Exception {
        TaskResponse toggled = new TaskResponse(1L, "Write tests", "JUnit tests", true, "nikoloz");
        when(taskService.toggleComplete(1L, "nikoloz")).thenReturn(toggled);

        mockMvc.perform(patch("/api/tasks/1/toggle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(true));
    }

    // DELETE /api/tasks/{id}

    @Test
    @WithMockUser(username = "nikoloz")
    @DisplayName("DELETE /api/tasks/{id} — returns 204 No Content")
    void deleteTask_returns204() throws Exception {
        doNothing().when(taskService).deleteTask(1L, "nikoloz");

        mockMvc.perform(delete("/api/tasks/1"))
                .andExpect(status().isNoContent());

        verify(taskService).deleteTask(1L, "nikoloz");
    }
}
