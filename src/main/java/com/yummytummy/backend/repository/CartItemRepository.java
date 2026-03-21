package com.yummytummy.backend.repository;

import com.yummytummy.backend.entity.CartItem;
import com.yummytummy.backend.entity.MenuItem;
import com.yummytummy.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Integer> {
    @Query("SELECT ci FROM CartItem ci JOIN FETCH ci.menuItem mi JOIN FETCH ci.user u WHERE u = :user")
    List<CartItem> findByUserWithMenuItemAndUser(@Param("user") User user);

    @Query("SELECT ci FROM CartItem ci JOIN FETCH ci.menuItem mi JOIN FETCH ci.user u WHERE u = :user AND mi = :menuItem")
    Optional<CartItem> findByUserAndMenuItemWithMenuItemAndUser(@Param("user") User user, @Param("menuItem") MenuItem menuItem);

    void deleteByUser(User user); // For clearing cart
}
