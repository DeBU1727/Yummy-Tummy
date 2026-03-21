package com.yummytummy.backend.dto;

// DTO for updating a user's password
public record PasswordUpdateDto(
    String currentPassword,
    String newPassword
) {
}
