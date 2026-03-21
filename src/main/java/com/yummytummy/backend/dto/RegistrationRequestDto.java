package com.yummytummy.backend.dto;

public record RegistrationRequestDto(
    String fullName,
    String email,
    String password
) {
}
