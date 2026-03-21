package com.yummytummy.backend.dto;

// DTO for sending user profile data to the client (omits password)
public record UserProfileDto(
    Long id,
    String fullName,
    String email,
    String profilePicturePath
) {
}
