package com.nikoloz.secureapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nikoloz.secureapp.config.SecurityConfig;
import com.nikoloz.secureapp.dto.RegisterRequest;
import com.nikoloz.secureapp.service.CustomUserDetailsService;
import com.nikoloz.secureapp.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthApiController.class)
@Import(SecurityConfig.class)
@DisplayName("AuthApiController — @WebMvcTest")
class AuthApiControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean UserService             userService;
    @MockBean CustomUserDetailsService customUserDetailsService;

    //  GET /api/auth/ping

    @Test
    @DisplayName("GET /api/auth/ping — returns 200 ok without authentication")
    void ping_returns200() throws Exception {
        mockMvc.perform(get("/api/auth/ping"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"));
    }

    //  POST /api/auth/register

    @Test
    @DisplayName("POST /api/auth/register — valid request returns 201 Created")
    void register_validRequest_returns201() throws Exception {
        RegisterRequest req = new RegisterRequest("newuser", "password1", "New User");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("newuser"));

        verify(userService).registerUser("newuser", "password1", "New User");
    }

    @Test
    @DisplayName("POST /api/auth/register — blank username returns 400")
    void register_blankUsername_returns400() throws Exception {
        RegisterRequest req = new RegisterRequest("", "password1", "Name");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).registerUser(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("POST /api/auth/register — short password returns 400")
    void register_shortPassword_returns400() throws Exception {
        RegisterRequest req = new RegisterRequest("validuser", "12", "Name");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/register — duplicate username returns 400")
    void register_duplicateUsername_returns400() throws Exception {
        RegisterRequest req = new RegisterRequest("existing", "password1", "Name");
        doThrow(new IllegalArgumentException("Username 'existing' is already taken."))
                .when(userService).registerUser("existing", "password1", "Name");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/register — Accept-Language ka returns 201 with Georgian message")
    void register_georgianLocale_usesMessageSource() throws Exception {
        RegisterRequest req = new RegisterRequest("newuser", "password1", "New User");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Accept-Language", "ka")
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }
}
