package com.yummytummy.backend.dto;

import com.yummytummy.backend.entity.Order;
import java.util.List;

// This DTO represents the complete request to place a new order
public record PlaceOrderRequestDto(
    Order.OrderType orderType,
    String customerName, // Customer Name
    String deliveryAddress,
    String contactNumber,
    Order.PaymentMethod paymentMethod,
    String couponCode, // Optional coupon code
    List<OrderItemRequestDto> items
) {
}
