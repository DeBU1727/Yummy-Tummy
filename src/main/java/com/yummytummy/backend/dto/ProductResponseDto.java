package com.yummytummy.backend.dto;

import java.math.BigDecimal;

public record ProductResponseDto(
    Integer id,
    String name,
    BigDecimal price,
    String description,
    String image,
    Integer categoryId,
    String categoryName
) {
}
