package com.nikoloz.secureapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TaskRequest(
        @NotBlank(message = "{validation.task.title.notblank}")
        @Size(min = 1, max = 200, message = "{validation.task.title.size}")
        String title,

        @Size(max = 1000, message = "{validation.task.description.size}")
        String description
) {}
