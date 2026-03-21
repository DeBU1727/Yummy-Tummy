package com.yummytummy.backend.repository;

import com.yummytummy.backend.entity.Order;
import com.yummytummy.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.Map;

public interface OrderRepository extends JpaRepository<Order, Integer> {
    List<Order> findByUserOrderByCreatedAtDesc(User user);

    @Query("SELECT SUM(o.totalPrice) FROM Order o WHERE o.createdAt >= :startDate")
    Double getSalesTotal(@Param("startDate") LocalDateTime startDate);

    @Query(value = "SELECT DATE(created_at) as date, SUM(total_price) as amount FROM orders WHERE created_at >= :startDate GROUP BY DATE(created_at) ORDER BY date", nativeQuery = true)
    List<Object[]> getDailySales(@Param("startDate") LocalDateTime startDate);

    @Query(value = "SELECT CASE WHEN user_id IS NULL THEN 'Offline' ELSE 'Online' END as source, COUNT(*) as count FROM orders WHERE created_at >= :startDate GROUP BY source", nativeQuery = true)
    List<Object[]> getOrderSourceRatio(@Param("startDate") LocalDateTime startDate);

    @Query(value = "SELECT mi.name, SUM(oi.quantity) as total_qty FROM order_items oi JOIN menu_items mi ON oi.menu_item_id = mi.id JOIN orders o ON oi.order_id = o.id WHERE o.created_at >= :startDate GROUP BY mi.name ORDER BY total_qty DESC LIMIT 10", nativeQuery = true)
    List<Object[]> getTopSellingItems(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT o.orderStatus as status, COUNT(o) as count FROM Order o WHERE o.createdAt >= :startDate AND o.createdAt <= :endDate GROUP BY o.orderStatus")
    List<Object[]> countOrdersByStatusAndDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT SUM(o.totalPrice) FROM Order o WHERE (o.orderStatus = 'DELIVERED' OR o.orderStatus = 'SERVED') AND o.createdAt >= :startDate AND o.createdAt <= :endDate")
    Double sumCompletedOrderTotalByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT SUM(o.totalPrice) FROM Order o WHERE (o.orderStatus = 'CANCELLED' OR o.orderStatus = 'REJECTED') AND o.createdAt >= :startDate AND o.createdAt <= :endDate")
    Double sumRefundedOrderTotalByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
