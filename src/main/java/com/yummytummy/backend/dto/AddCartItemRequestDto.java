package com.yummytummy.backend.dto;

public record AddCartItemRequestDto(
    Integer menuItemId,
    int quantity
) {
}
