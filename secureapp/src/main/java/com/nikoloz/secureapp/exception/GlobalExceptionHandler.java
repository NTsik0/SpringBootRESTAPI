package com.nikoloz.secureapp.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * here is centralized, global, we can say everything in one place, error handling for the whole application.
 * every error response is localised: the Accept-Language header on the incoming
 * request determines which messages.properties file Spring resolves messages from.
 * structured logging is used throughout  no System.out.println().
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    // ── Bean validation errors (@Valid on controller params) ─────────────

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(
            MethodArgumentNotValidException ex, Locale locale) {

        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.toList());

        log.warn("Validation failed for request — {} field error(s): {}", errors.size(), errors);

        String message = messageSource.getMessage("error.validation", null, locale);

        return ResponseEntity.badRequest().body(Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", HttpStatus.BAD_REQUEST.value(),
                "error", message,
                "details", errors
        ));
    }

    // ── Domain rule violations (e.g. "username already taken") ───────────

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(
            IllegalArgumentException ex, Locale locale) {

        log.warn("Illegal argument exception: {}", ex.getMessage());

        String message = messageSource.getMessage(
                "error.bad.request", new Object[]{ex.getMessage()}, locale);

        return ResponseEntity.badRequest().body(Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", HttpStatus.BAD_REQUEST.value(),
                "error", message
        ));
    }

    // ── Access denied ─────────────────────────────────────────────────────

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(Locale locale) {

        log.warn("Access denied — insufficient privileges");

        String message = messageSource.getMessage("error.access.denied", null, locale);

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", HttpStatus.FORBIDDEN.value(),
                "error", message
        ));
    }

    // ── Catch-all ─────────────────────────────────────────────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(
            Exception ex, Locale locale) {

        log.error("Unexpected error occurred: {}", ex.getMessage(), ex);

        String message = messageSource.getMessage("error.internal", null, locale);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "error", message
        ));
    }
}
