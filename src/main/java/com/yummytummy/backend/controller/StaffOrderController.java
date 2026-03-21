package com.yummytummy.backend.controller;

import com.yummytummy.backend.dto.OrderResponseDto;
import com.yummytummy.backend.dto.PlaceOrderRequestDto;
import com.yummytummy.backend.entity.Order;
import com.yummytummy.backend.repository.OrderRepository;
import com.yummytummy.backend.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/staff")
public class StaffOrderController {

    private final OrderService orderService;
    private final OrderRepository orderRepository;

    public StaffOrderController(OrderService orderService, OrderRepository orderRepository) {
        this.orderService = orderService;
        this.orderRepository = orderRepository;
    }

    // Get all orders for staff panel
    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponseDto>> getAllOrders() {
        // We use the existing OrderService conversion logic
        // This is a simple implementation, usually you'd have a separate service method
        List<OrderResponseDto> orders = orderRepository.findAll().stream()
                .map(this::convertToDto) // Helper below or in service
                .sorted((a, b) -> b.createdAt().compareTo(a.createdAt()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(orders);
    }

    // Get specific order by ID
    @GetMapping("/orders/{id}")
    public ResponseEntity<OrderResponseDto> getOrderById(@PathVariable Integer id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        return ResponseEntity.ok(convertToDto(order));
    }

    // Update order status
    @PutMapping("/orders/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Integer id, @RequestBody String status) {
        try {
            // Remove quotes if any from raw body string
            String statusStr = status.replace("\"", "");
            Order.OrderStatus newStatus = Order.OrderStatus.valueOf(statusStr);
            OrderResponseDto updatedOrder = orderService.updateOrderStatus(id, newStatus);
            return ResponseEntity.ok(updatedOrder);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Update payment status
    @PutMapping("/orders/{id}/payment-status")
    public ResponseEntity<?> updatePaymentStatus(@PathVariable Integer id, @RequestBody String status) {
        try {
            String statusStr = status.replace("\"", "");
            Order.PaymentStatus newStatus = Order.PaymentStatus.valueOf(statusStr);
            OrderResponseDto updatedOrder = orderService.updatePaymentStatus(id, newStatus);
            return ResponseEntity.ok(updatedOrder);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Place order on behalf of customer (Public - No JWT required)
    @PostMapping("/orders/place")
    public ResponseEntity<OrderResponseDto> placeStaffOrder(@RequestBody PlaceOrderRequestDto requestDto) {
        try {
            OrderResponseDto newOrder = orderService.placeOrder("staff@yummytummy.com", requestDto);
            return ResponseEntity.ok(newOrder);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    private OrderResponseDto convertToDto(Order order) {
        // We need to map Order to OrderResponseDto manually or use a shared method.
        // I will add a public convertToDto or a new method in OrderService for this.
        // For now, let's just use a basic mapping since we have the data.
        List<com.yummytummy.backend.dto.OrderItemResponseDto> itemDtos = order.getOrderItems().stream()
                .map(item -> new com.yummytummy.backend.dto.OrderItemResponseDto(
                        item.getId(),
                        item.getMenuItem().getId(),
                        item.getMenuItem().getName(),
                        item.getPrice(),
                        item.getQuantity()
                )).collect(Collectors.toList());

        return new OrderResponseDto(
                order.getId(),
                order.getOrderType(),
                order.getCustomerName(),
                order.getDeliveryAddress(),
                order.getContactNumber(),
                order.getSubtotal(),
                order.getDiscountAmount(),
                order.getCouponCode(),
                order.getGstAmount(),
                order.getPaymentMethod(),
                order.getPaymentStatus(),
                order.getOrderStatus(),
                order.getTotalPrice(),
                order.getCreatedAt(),
                itemDtos
        );
    }
}
