package com.nikoloz.secureapp.dto;

import com.nikoloz.secureapp.model.Task;

public record TaskResponse(
        Long    id,
        String  title,
        String  description,
        boolean completed,
        String  ownerUsername
) {
    public static TaskResponse from(Task t) {
        return new TaskResponse(
                t.getId(),
                t.getTitle(),
                t.getDescription(),
                t.isCompleted(),
                t.getOwner().getUsername()
        );
    }
}
