package com.yummytummy.backend.dto;

public record OrderStatusCountDto(
    String status,
    Long count
) {}
