package com.springlms.backend.dto;

import com.springlms.backend.model.user.Role;
import com.springlms.backend.model.user.State;

public record LoginResponse(
        Long userId,
        String email,
        Role role,
        State state,
        String message
) {
}