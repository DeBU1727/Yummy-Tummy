package com.yummytummy.backend.controller;

import com.yummytummy.backend.dto.AddCartItemRequestDto;
import com.yummytummy.backend.dto.CartItemResponseDto;
import com.yummytummy.backend.entity.CartItem;
import com.yummytummy.backend.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<List<CartItemResponseDto>> getCart(Authentication authentication) {
        String userEmail = authentication.getName();
        List<CartItemResponseDto> cartItems = cartService.getCartItems(userEmail);
        return ResponseEntity.ok(cartItems);
    }

    @PostMapping("/add")
    public ResponseEntity<CartItemResponseDto> addOrUpdateItem(Authentication authentication, @RequestBody AddCartItemRequestDto requestDto) {
        String userEmail = authentication.getName();
        CartItemResponseDto cartItem = cartService.addOrUpdateCartItem(userEmail, requestDto);
        return ResponseEntity.ok(cartItem);
    }

    @DeleteMapping("/remove/{menuItemId}")
    public ResponseEntity<?> removeItem(Authentication authentication, @PathVariable Integer menuItemId) {
        String userEmail = authentication.getName();
        cartService.removeCartItem(userEmail, menuItemId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/clear")
    public ResponseEntity<?> clearCart(Authentication authentication) {
        String userEmail = authentication.getName();
        cartService.clearCart(userEmail);
        return ResponseEntity.ok().build();
    }
}
