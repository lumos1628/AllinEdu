package com.edufast.application.dto;

public record LoginResponse(
        String token,
        Long userId,
        String name,
        String email,
        String role,
        String roleScope,
        Short educationLevelId,
        Long supervisorUserId) {
}
