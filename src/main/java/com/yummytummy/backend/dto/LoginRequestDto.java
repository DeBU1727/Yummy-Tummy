package com.yummytummy.backend.dto;

public record LoginRequestDto(
    String email,
    String password
) {
}
