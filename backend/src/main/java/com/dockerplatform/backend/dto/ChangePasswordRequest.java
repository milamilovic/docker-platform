package com.dockerplatform.backend.dto;

public record ChangePasswordRequest(
        String username,
        String password,
        String newPassword
) {}