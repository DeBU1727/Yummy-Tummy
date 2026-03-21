package com.yummytummy.backend.controller;

import com.yummytummy.backend.dto.AnalyticsResponseDto;
import com.yummytummy.backend.dto.FinancialSummaryDto;
import com.yummytummy.backend.dto.OrderStatusCountDto;
import com.yummytummy.backend.repository.OrderRepository;
import com.yummytummy.backend.entity.Order; // Import Order entity
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/analytics")
public class AdminDashboardController {

    private final OrderRepository orderRepository;

    public AdminDashboardController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @GetMapping("/sales-overview")
    public ResponseEntity<Map<String, Object>> getSalesOverview(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate) {
        
        Double total = orderRepository.getSalesTotal(startDate);
        List<Object[]> dailyData = orderRepository.getDailySales(startDate);
        
        List<Map<String, Object>> chartData = dailyData.stream().map(row -> {
            Map<String, Object> map = new HashMap<>();
            map.put("date", row[0].toString());
            map.put("amount", row[1]);
            return map;
        }).collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("totalSales", total != null ? total : 0.0);
        response.put("chartData", chartData);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/source-ratio")
    public ResponseEntity<List<Map<String, Object>>> getSourceRatio(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate) {
        
        List<Object[]> ratioData = orderRepository.getOrderSourceRatio(startDate);
        
        List<Map<String, Object>> response = ratioData.stream().map(row -> {
            Map<String, Object> map = new HashMap<>();
            map.put("source", row[0]);
            map.put("count", row[1]);
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/top-sellers")
    public ResponseEntity<List<Map<String, Object>>> getTopSellers(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate) {
        
        List<Object[]> topItems = orderRepository.getTopSellingItems(startDate);
        
        List<Map<String, Object>> response = topItems.stream().map(row -> {
            Map<String, Object> map = new HashMap<>();
            map.put("name", row[0]);
            map.put("quantity", row[1]);
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/summary")
    public ResponseEntity<AnalyticsResponseDto> getAnalyticsSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        // Order Status Ratio
        List<Object[]> statusCountsRaw = orderRepository.countOrdersByStatusAndDateRange(startDate, endDate);
        List<OrderStatusCountDto> orderStatusRatio = statusCountsRaw.stream()
                .map(row -> new OrderStatusCountDto(((Order.OrderStatus) row[0]).name(), (Long) row[1]))
                .collect(Collectors.toList());

        // Financial Summary
        Double totalEarnings = orderRepository.sumCompletedOrderTotalByDateRange(startDate, endDate);
        Double totalRefunds = orderRepository.sumRefundedOrderTotalByDateRange(startDate, endDate);
        FinancialSummaryDto financialSummary = new FinancialSummaryDto(
            totalEarnings != null ? totalEarnings : 0.0,
            totalRefunds != null ? totalRefunds : 0.0
        );

        AnalyticsResponseDto responseDto = new AnalyticsResponseDto(orderStatusRatio, financialSummary);
        return ResponseEntity.ok(responseDto);
    }
}
