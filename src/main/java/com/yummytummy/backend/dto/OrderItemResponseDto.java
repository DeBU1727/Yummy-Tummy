package com.yummytummy.backend.dto;

import java.math.BigDecimal;

public record OrderItemResponseDto(
    Integer id,
    Integer menuItemId,
    String name,
    BigDecimal price,
    int quantity
) {
}
