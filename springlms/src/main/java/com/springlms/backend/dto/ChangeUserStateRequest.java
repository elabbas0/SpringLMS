package com.springlms.backend.dto;

import com.springlms.backend.model.user.State;

public record ChangeUserStateRequest(
        State state
) {
}