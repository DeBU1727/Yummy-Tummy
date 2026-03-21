package com.yummytummy.backend.dto;

import com.yummytummy.backend.entity.Order;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponseDto(
    Integer id,
    Order.OrderType orderType,
    String customerName,
    String deliveryAddress,
    String contactNumber,
    BigDecimal subtotal,
    BigDecimal discountAmount,
    String couponCode,
    BigDecimal gstAmount,
    Order.PaymentMethod paymentMethod,
    Order.PaymentStatus paymentStatus,
    Order.OrderStatus orderStatus,
    BigDecimal totalPrice,
    LocalDateTime createdAt,
    List<OrderItemResponseDto> items
) {
}
