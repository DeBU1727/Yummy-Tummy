package com.yummytummy.backend.dto;

import java.util.List;

public record AnalyticsResponseDto(
    List<OrderStatusCountDto> orderStatusRatio,
    FinancialSummaryDto financialSummary
) {}
