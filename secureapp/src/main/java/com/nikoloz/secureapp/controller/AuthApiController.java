package com.nikoloz.secureapp.controller;

import com.nikoloz.secureapp.dto.RegisterRequest;
import com.nikoloz.secureapp.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

    private final UserService userService;
    private final MessageSource messageSource;

    public AuthApiController(UserService userService, MessageSource messageSource) {
        this.userService = userService;
        this.messageSource = messageSource;
    }

    @GetMapping("/ping")
    public ResponseEntity<Map<String, String>> ping() {
        log.debug("GET /api/auth/ping called");
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(
            @Valid @RequestBody RegisterRequest request,
            Locale locale) {

        log.info("API registration attempt for username '{}'", request.username());

        userService.registerUser(
                request.username(),
                request.password(),
                request.displayName()
        );

        String message = messageSource.getMessage("user.registered", null, locale);

        log.info("API user registered successfully with username '{}'", request.username());

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", message,
                "username", request.username()
        ));
    }
}