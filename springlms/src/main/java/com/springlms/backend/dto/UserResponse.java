package com.springlms.backend.dto;

import com.springlms.backend.model.user.Role;
import com.springlms.backend.model.user.State;

public record UserResponse(
        Long id,
        String email,
        Role role,
        State state
) {
}