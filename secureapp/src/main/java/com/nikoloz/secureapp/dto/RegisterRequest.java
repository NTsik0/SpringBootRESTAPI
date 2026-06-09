package com.nikoloz.secureapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank(message = "{validation.username.notblank}")
        @Size(min = 3, max = 50, message = "{validation.username.size}")
        String username,

        @NotBlank(message = "{validation.password.notblank}")
        @Size(min = 6, max = 100, message = "{validation.password.size}")
        String password,

        @NotBlank(message = "{validation.displayName.notblank}")
        String displayName
) {
}