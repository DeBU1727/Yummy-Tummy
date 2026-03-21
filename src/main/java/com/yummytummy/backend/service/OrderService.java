package com.yummytummy.backend.service;

import com.yummytummy.backend.dto.OrderItemResponseDto;
import com.yummytummy.backend.dto.OrderResponseDto;
import com.yummytummy.backend.dto.PlaceOrderRequestDto;
import com.yummytummy.backend.entity.MenuItem;
import com.yummytummy.backend.entity.Order;
import com.yummytummy.backend.entity.OrderItem;
import com.yummytummy.backend.repository.MenuItemRepository;
import com.yummytummy.backend.repository.OrderRepository;
import com.yummytummy.backend.repository.OfferRepository;
import com.yummytummy.backend.repository.UserRepository;
import com.yummytummy.backend.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final MenuItemRepository menuItemRepository;
    private final UserRepository userRepository;
    private final EmailService emailService; // Injected EmailService

    @Autowired
    public OrderService(OrderRepository orderRepository, MenuItemRepository menuItemRepository, UserRepository userRepository, OfferRepository offerRepository, EmailService emailService) {
        this.orderRepository = orderRepository;
        this.menuItemRepository = menuItemRepository;
        this.userRepository = userRepository;
        this.offerRepository = offerRepository;
        this.emailService = emailService; // Assigned EmailService
    }

    private final OfferRepository offerRepository;

    @Transactional
    public OrderResponseDto placeOrder(String userEmail, PlaceOrderRequestDto requestDto) {
        User user = null;
        if (userEmail != null && !userEmail.equals("staff@yummytummy.com")) {
            user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));
        }

        Order order = new Order();
        if (user != null) {
            order.setUser(user);
        }
        
        // Priority: Name from request (Staff manual entry) > User's full name
        String name = requestDto.customerName();
        if ((name == null || name.isEmpty()) && user != null) {
            name = user.getFullName();
        }
        order.setCustomerName(name);

        order.setOrderType(requestDto.orderType());
        order.setPaymentMethod(requestDto.paymentMethod());

        if (requestDto.orderType() == Order.OrderType.DELIVERY) {
            if (requestDto.deliveryAddress() == null || requestDto.contactNumber() == null) {
                throw new IllegalArgumentException("Delivery address and contact number are required for delivery orders.");
            }
            order.setDeliveryAddress(requestDto.deliveryAddress());
            order.setContactNumber(requestDto.contactNumber());
        }

        List<Integer> menuItemIds = requestDto.items().stream().map(dto -> dto.menuItemId()).collect(Collectors.toList());
        Map<Integer, MenuItem> menuItemMap = menuItemRepository.findAllById(menuItemIds).stream()
                .collect(Collectors.toMap(MenuItem::getId, Function.identity()));

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (var itemDto : requestDto.items()) {
            MenuItem menuItem = menuItemMap.get(itemDto.menuItemId());
            if (menuItem == null) {
                throw new IllegalArgumentException("Menu item not found with id: " + itemDto.menuItemId());
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setMenuItem(menuItem);
            orderItem.setQuantity(itemDto.quantity());
            orderItem.setPrice(menuItem.getPrice()); // Price at time of order
            orderItems.add(orderItem);

            subtotal = subtotal.add(menuItem.getPrice().multiply(BigDecimal.valueOf(itemDto.quantity())));
        }

        order.setOrderItems(orderItems);
        order.setSubtotal(subtotal);

        BigDecimal discountAmount = BigDecimal.ZERO;
        if (requestDto.couponCode() != null && !requestDto.couponCode().isEmpty()) {
            var offerOpt = offerRepository.findByOfferCode(requestDto.couponCode());
            if (offerOpt.isPresent()) {
                var offer = offerOpt.get();
                order.setCouponCode(offer.getOfferCode());
                BigDecimal discountRate = BigDecimal.valueOf(offer.getDiscountPercentage()).divide(new BigDecimal("100"));
                discountAmount = subtotal.multiply(discountRate);
            }
        }
        order.setDiscountAmount(discountAmount);

        BigDecimal amountAfterDiscount = subtotal.subtract(discountAmount);
        BigDecimal gstRate = new BigDecimal("0.18");
        BigDecimal gstAmount = amountAfterDiscount.multiply(gstRate);
        order.setGstAmount(gstAmount);
        order.setTotalPrice(amountAfterDiscount.add(gstAmount));
        
        // Payment Status Logic:
        // Set to Pending only if it's Delivery + Cash. All other cases (Dine-In, Online, Card) are marked Completed.
        if (requestDto.orderType() == Order.OrderType.DELIVERY && requestDto.paymentMethod() == Order.PaymentMethod.CASH) {
            order.setPaymentStatus(Order.PaymentStatus.PENDING);
        } else {
            order.setPaymentStatus(Order.PaymentStatus.COMPLETED);
        }
        
        Order savedOrder = orderRepository.save(order);
        
        // Send order confirmation email
        // Fetch order items and menu items again to ensure they are fully loaded if LAZY fetched
        // Or ensure the initial save loads them. For now, assume savedOrder has them.
        emailService.sendOrderConfirmationEmail(savedOrder);
        
        return convertToDto(savedOrder);
    }

    public Integer validateCoupon(String code) {
        return offerRepository.findByOfferCode(code)
                .map(com.yummytummy.backend.entity.Offer::getDiscountPercentage)
                .orElseThrow(() -> new RuntimeException("Invalid coupon code"));
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDto> getUserOrders(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return orderRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderResponseDto getOrderDetails(Integer orderId, String userEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("Unauthorized to view this order");
        }

        return convertToDto(order);
    }

    @Transactional
    public OrderResponseDto cancelOrder(Integer orderId, String userEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("Unauthorized to cancel this order");
        }

        if (order.getOrderStatus() != Order.OrderStatus.PENDING) {
            throw new RuntimeException("Only pending orders can be cancelled");
        }

        order.setOrderStatus(Order.OrderStatus.CANCELLED);
        Order savedOrder = orderRepository.save(order);
        emailService.sendOrderCancellationEmail(savedOrder); // Send cancellation email
        return convertToDto(savedOrder);
    }

    @Transactional
    public OrderResponseDto updateOrderStatus(Integer orderId, Order.OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // If the order is being rejected, send a rejection email
        if (newStatus == Order.OrderStatus.REJECTED) {
            emailService.sendOrderRejectedEmail(order);
        }

        // Validation Rule: If status is being changed to DELIVERED, payment must be COMPLETED
        if (newStatus == Order.OrderStatus.DELIVERED && order.getPaymentStatus() == Order.PaymentStatus.PENDING) {
            throw new RuntimeException("Cannot mark order as Delivered while payment is still Pending.");
        }

        order.setOrderStatus(newStatus);
        Order savedOrder = orderRepository.save(order);
        return convertToDto(savedOrder);
    }

    @Transactional
    public OrderResponseDto updatePaymentStatus(Integer orderId, Order.PaymentStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setPaymentStatus(newStatus);
        Order savedOrder = orderRepository.save(order);
        return convertToDto(savedOrder);
    }

    @Transactional
    public void deleteOrder(Integer orderId, String userEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("Unauthorized to delete this order");
        }

        Order.OrderStatus status = order.getOrderStatus();
        if (status == Order.OrderStatus.CANCELLED || status == Order.OrderStatus.DELIVERED || 
            status == Order.OrderStatus.SERVED || status == Order.OrderStatus.REJECTED) {
            orderRepository.delete(order);
        } else {
            throw new RuntimeException("Only cancelled, delivered, served, or rejected orders can be deleted from history");
        }
    }

    private OrderResponseDto convertToDto(Order order) {
        List<OrderItemResponseDto> itemDtos = order.getOrderItems().stream()
                .map(this::convertToItemDto)
                .collect(Collectors.toList());

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

    private OrderItemResponseDto convertToItemDto(OrderItem item) {
        return new OrderItemResponseDto(
                item.getId(),
                item.getMenuItem().getId(),
                item.getMenuItem().getName(),
                item.getPrice(),
                item.getQuantity()
        );
    }
}
