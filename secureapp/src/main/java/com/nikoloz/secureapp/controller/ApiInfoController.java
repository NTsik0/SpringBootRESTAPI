package com.nikoloz.secureapp.controller;

import com.nikoloz.secureapp.config.AppSettingsProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * public REST endpoint that exposes application metadata.
 * this here demonstrates two Assignment-2 requirements together:
 *   1. Injecting AppSettingsProperties to drive dynamic, profile-aware responses.
 *   2. Returning a localised welcome message based on the Accept-Language header.
 * for test localisation we gotta use next:
 *   curl http://localhost:8080/api/info -H "Accept-Language: en"
 *   curl http://localhost:8080/api/info -H "Accept-Language: ka"
 * when app.settings.debug-mode=true (dev profile) the response also includes the resolved external service URL
 * developers can quickly verify which that the config is active.
 */
@Slf4j
@RestController
@RequestMapping("/api")
public class ApiInfoController {

    private final AppSettingsProperties settings;
    private final MessageSource         messageSource;

    public ApiInfoController(AppSettingsProperties settings, MessageSource messageSource) {
        this.settings      = settings;
        this.messageSource = messageSource;
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info(Locale locale) {
        log.info("GET /api/info — title: '{}', locale: {}",
                settings.getTitle(), locale.getLanguage());

        String welcomeMessage = messageSource.getMessage("api.welcome", null, locale);

        // Use LinkedHashMap to guarantee a consistent key order in the JSON output
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("application", settings.getTitle());
        body.put("contact",     settings.getContactEmail());
        body.put("maxPageSize", settings.getPaginationLimit());
        body.put("message",     welcomeMessage);
        body.put("timestamp",   LocalDateTime.now().toString());

        // Extra diagnostic info — only present when debug-mode is true (dev profile)
        if (settings.isDebugMode()) {
            log.debug("Debug mode ON — appending extra diagnostic fields to /api/info response");
            body.put("debugMode",          true);
            body.put("externalServiceUrl", settings.getExternalServiceUrl());
        }

        log.debug("Returning /api/info payload with {} keys", body.size());
        return ResponseEntity.ok(body);
    }
}
