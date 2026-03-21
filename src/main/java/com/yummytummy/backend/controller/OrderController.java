package com.yummytummy.backend.controller;

import com.yummytummy.backend.dto.OrderResponseDto;
import com.yummytummy.backend.dto.PlaceOrderRequestDto;
import com.yummytummy.backend.entity.Order;
import com.yummytummy.backend.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/place")
    public ResponseEntity<OrderResponseDto> placeOrder(@RequestBody PlaceOrderRequestDto requestDto) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            OrderResponseDto newOrder = orderService.placeOrder(userEmail, requestDto);
            return new ResponseEntity<>(newOrder, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping
    public ResponseEntity<List<OrderResponseDto>> getUserOrders() {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        List<OrderResponseDto> orders = orderService.getUserOrders(userEmail);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDto> getOrderById(@PathVariable Integer id) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        OrderResponseDto order = orderService.getOrderDetails(id, userEmail);
        return ResponseEntity.ok(order);
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<OrderResponseDto> cancelOrder(@PathVariable Integer id) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        OrderResponseDto updatedOrder = orderService.cancelOrder(id, userEmail);
        return ResponseEntity.ok(updatedOrder);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Integer id) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        orderService.deleteOrder(id, userEmail);
        return ResponseEntity.ok().build();
    }
}
