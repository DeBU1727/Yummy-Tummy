package com.yummytummy.backend.dto;

import java.math.BigDecimal;

public record CartItemResponseDto(
    Integer id, // Cart item ID
    Integer menuItemId,
    String name,
    BigDecimal price,
    String image,
    int quantity
) {
}
