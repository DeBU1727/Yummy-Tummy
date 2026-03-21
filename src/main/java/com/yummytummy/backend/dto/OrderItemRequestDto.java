package com.yummytummy.backend.dto;

// This DTO represents a single item in the incoming order request
public record OrderItemRequestDto(
    Integer menuItemId,
    int quantity
) {
}
