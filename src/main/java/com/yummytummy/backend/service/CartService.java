package com.yummytummy.backend.service;

import com.yummytummy.backend.dto.AddCartItemRequestDto;
import com.yummytummy.backend.dto.CartItemResponseDto; // Import the new DTO
import com.yummytummy.backend.entity.CartItem;
import com.yummytummy.backend.entity.MenuItem;
import com.yummytummy.backend.entity.User;
import com.yummytummy.backend.repository.CartItemRepository;
import com.yummytummy.backend.repository.MenuItemRepository;
import com.yummytummy.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final MenuItemRepository menuItemRepository;

    public CartService(CartItemRepository cartItemRepository, UserRepository userRepository, MenuItemRepository menuItemRepository) {
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
        this.menuItemRepository = menuItemRepository;
    }

    @Transactional
    public List<CartItemResponseDto> getCartItems(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return cartItemRepository.findByUserWithMenuItemAndUser(user).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Helper method to convert CartItem entity to DTO
    private CartItemResponseDto convertToDto(CartItem cartItem) {
        MenuItem menuItem = cartItem.getMenuItem();
        return new CartItemResponseDto(
                cartItem.getId(),
                menuItem.getId(),
                menuItem.getName(),
                menuItem.getPrice(),
                menuItem.getImage(),
                cartItem.getQuantity()
        );
    }

    @Transactional
    public CartItemResponseDto addOrUpdateCartItem(String userEmail, AddCartItemRequestDto requestDto) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        MenuItem menuItem = menuItemRepository.findById(requestDto.menuItemId())
                .orElseThrow(() -> new RuntimeException("Menu item not found"));

        Optional<CartItem> existingCartItem = cartItemRepository.findByUserAndMenuItemWithMenuItemAndUser(user, menuItem);

        CartItem cartItem;
        if (existingCartItem.isPresent()) {
            cartItem = existingCartItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + requestDto.quantity());
            if (cartItem.getQuantity() <= 0) {
                cartItemRepository.delete(cartItem);
                return null; // Item removed from cart
            }
        } else {
            if (requestDto.quantity() <= 0) {
                return null; // Cannot add 0 or less quantity if item not in cart
            }
            cartItem = new CartItem();
            cartItem.setUser(user);
            cartItem.setMenuItem(menuItem);
            cartItem.setQuantity(requestDto.quantity());
        }
        CartItem savedCartItem = cartItemRepository.save(cartItem);
        return convertToDto(savedCartItem);
    }

    @Transactional
    public void removeCartItem(String userEmail, Integer menuItemId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new RuntimeException("Menu item not found"));

        cartItemRepository.findByUserAndMenuItemWithMenuItemAndUser(user, menuItem)
                .ifPresent(cartItemRepository::delete);
    }

    @Transactional
    public void clearCart(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        cartItemRepository.deleteByUser(user);
    }
}
