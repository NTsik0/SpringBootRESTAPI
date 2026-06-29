package com.nikoloz.secureapp.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nikoloz.secureapp.dto.TaskRequest;
import com.nikoloz.secureapp.model.AppUser;
import com.nikoloz.secureapp.model.UserRole;
import com.nikoloz.secureapp.repository.TaskRepository;
import com.nikoloz.secureapp.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@TestInstance(PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Task REST API — full integration tests")
class TaskIntegrationTest {

    @Autowired MockMvc         mockMvc;
    @Autowired ObjectMapper    objectMapper;
    @Autowired TaskRepository  taskRepository;
    @Autowired UserRepository  userRepository;
    @Autowired PasswordEncoder passwordEncoder;

    private Long createdTaskId;

    @BeforeEach
    void ensureUserExists() {
        if (!userRepository.existsByUsername("integrationuser")) {
            userRepository.save(new AppUser(
                    "integrationuser",
                    passwordEncoder.encode("pass"),
                    UserRole.ROLE_USER,
                    "Integration User"
            ));
        }
    }

    // POST /api/tasks — create

    @Test
    @Order(1)
    @WithMockUser(username = "integrationuser", roles = "USER")
    @DisplayName("POST /api/tasks — creates task and persists to DB")
    void createTask_persistsToDB() throws Exception {
        TaskRequest req = new TaskRequest("Integration task", "Created in test");

        String response = mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Integration task"))
                .andExpect(jsonPath("$.completed").value(false))
                .andExpect(jsonPath("$.ownerUsername").value("integrationuser"))
                .andReturn().getResponse().getContentAsString();

        createdTaskId = objectMapper.readTree(response).get("id").asLong();
        assertThat(taskRepository.findById(createdTaskId)).isPresent();
    }

    // GET /api/tasks — list

    @Test
    @Order(2)
    @WithMockUser(username = "integrationuser", roles = "USER")
    @DisplayName("GET /api/tasks — returns tasks for authenticated user")
    void getMyTasks_returnsList() throws Exception {
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // PATCH toggle

    @Test
    @Order(3)
    @WithMockUser(username = "integrationuser", roles = "USER")
    @DisplayName("PATCH /api/tasks/{id}/toggle — flips completed flag in DB")
    void toggleComplete_updatesDB() throws Exception {
        Assumptions.assumeTrue(createdTaskId != null, "depends on create test");

        mockMvc.perform(patch("/api/tasks/" + createdTaskId + "/toggle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(true));

        assertThat(taskRepository.findById(createdTaskId)
                .orElseThrow().isCompleted()).isTrue();
    }

    // PUT /api/tasks/{id} — update

    @Test
    @Order(4)
    @WithMockUser(username = "integrationuser", roles = "USER")
    @DisplayName("PUT /api/tasks/{id} — updates title and description")
    void updateTask_persistsChanges() throws Exception {
        Assumptions.assumeTrue(createdTaskId != null, "depends on create test");
        TaskRequest update = new TaskRequest("Updated title", "Updated desc");

        mockMvc.perform(put("/api/tasks/" + createdTaskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated title"));

        assertThat(taskRepository.findById(createdTaskId)
                .orElseThrow().getTitle()).isEqualTo("Updated title");
    }

    // DELETE /api/tasks/{id}

    @Test
    @Order(5)
    @WithMockUser(username = "integrationuser", roles = "USER")
    @DisplayName("DELETE /api/tasks/{id} — removes task from DB")
    void deleteTask_removesFromDB() throws Exception {
        Assumptions.assumeTrue(createdTaskId != null, "depends on create test");

        mockMvc.perform(delete("/api/tasks/" + createdTaskId))
                .andExpect(status().isNoContent());

        assertThat(taskRepository.findById(createdTaskId)).isEmpty();
    }

    //  Authorization boundary

    @Test
    @Order(6)
    @WithMockUser(username = "otheruser", roles = "USER")
    @DisplayName("GET /api/tasks/{id} — other user cannot see someone else's task (404)")
    void getTask_wrongOwner_returns404() throws Exception {
        AppUser owner = userRepository.findByUsername("integrationuser").orElseThrow();
        com.nikoloz.secureapp.model.Task t = new com.nikoloz.secureapp.model.Task("Private", "secret", owner);
        Long id = taskRepository.save(t).getId();

        mockMvc.perform(get("/api/tasks/" + id))
                .andExpect(status().isNotFound());
    }

    //  Actuator

    @Test
    @Order(7)
    @DisplayName("GET /actuator/health — public endpoint returns UP")
    void actuatorHealth_isUp() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @Order(8)
    @DisplayName("GET /actuator/info — public endpoint returns app info")
    void actuatorInfo_returnsInfo() throws Exception {
        mockMvc.perform(get("/actuator/info"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(9)
    @DisplayName("GET /actuator/metrics — requires ADMIN role, returns 403 for USER")
    void actuatorMetrics_requiresAdmin() throws Exception {
        mockMvc.perform(get("/actuator/metrics")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                .user("regularuser").roles("USER")))
                .andExpect(status().isForbidden());
    }
}
