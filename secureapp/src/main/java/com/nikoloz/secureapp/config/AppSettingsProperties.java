package com.nikoloz.secureapp.config;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Strongly-typed, validated configuration properties.
 * All values are loaded from application.yml (or profile-specific overrides)
 * under the "app.settings" prefix.
 * Lombok @Data generates getters, setters, equals, hashCode and toString.
 */
@Data
@Validated
@ConfigurationProperties(prefix = "app.settings")
public class AppSettingsProperties {

    /**
     * Human-readable application name shown in API metadata response and included in the log headers.
     */
    @NotBlank(message = "Application title must not be blank")
    private String title;

    /**
     * Maximum number of records returned per paginated list endpoint.
     * Prevents accidental full-table scans via the API.
     */
    @Min(value = 1,   message = "Pagination limit must be at least 1")
    @Max(value = 200, message = "Pagination limit must not exceed 200")
    private int paginationLimit;

    /**
     * Base URL of any downstream / external service this application calls.
     * It is, they are: Different per environment (mock URL in dev, real URL in prod).
     */
    @NotBlank(message = "External service URL must not be blank")
    private String externalServiceUrl;

    /**
     * Contact mail embedded in error responses and exposed through the ..api..info metadata endpoint.
     */
    @NotBlank
    @Email(message = "Contact email must be a valid e-mail address")
    private String contactEmail;

    /**
     * Feature flag: when true, API responses include extra diagnostic fields.  Must be false in production.
     */
    private boolean debugMode;
}
