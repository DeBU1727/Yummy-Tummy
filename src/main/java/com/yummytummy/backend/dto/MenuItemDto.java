package com.yummytummy.backend.dto;

import java.math.BigDecimal;

public record MenuItemDto(
    Integer id,
    String name,
    BigDecimal price,
    String description,
    String image
) {
}
