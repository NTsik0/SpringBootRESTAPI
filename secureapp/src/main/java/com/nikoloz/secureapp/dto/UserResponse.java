package com.nikoloz.secureapp.dto;

import com.nikoloz.secureapp.model.AppUser;

public record UserResponse(
        Long    id,
        String  username,
        String  displayName,
        String  role,
        boolean enabled
) {
    public static UserResponse from(AppUser u) {
        return new UserResponse(u.getId(), u.getUsername(), u.getDisplayName(),
                u.getRole().name(), u.isEnabled());
    }
}
