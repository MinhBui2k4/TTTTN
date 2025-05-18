package com.techstore.vanminh.controller;

import com.techstore.vanminh.dto.CartDTO;
import com.techstore.vanminh.dto.CartItemDTO;
import com.techstore.vanminh.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users/cart")
@PreAuthorize("hasRole('ROLE_USER')")
public class CartController {

    @Autowired
    private CartService cartService;

    // @GetMapping
    // public ResponseEntity<BaseResponse<CartItemDTO>> getCart(
    // @RequestParam(defaultValue = "0") int pageNumber,
    // @RequestParam(defaultValue = "10") int pageSize,
    // @RequestParam(defaultValue = "id") String sortBy,
    // @RequestParam(defaultValue = "asc") String sortOrder) {
    // BaseResponse<CartItemDTO> response = cartService.getCart(pageNumber,
    // pageSize, sortBy, sortOrder);
    // return ResponseEntity.ok(response);
    // }

    @GetMapping
    public ResponseEntity<CartDTO> getCart() {
        return ResponseEntity.ok(cartService.getCart());
    }

    @PostMapping("/items")
    public ResponseEntity<CartDTO> addItemToCart(@Valid @RequestBody CartItemDTO cartItemDTO) {
        return ResponseEntity.ok(cartService.addItemToCart(cartItemDTO));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartDTO> updateCartItem(@PathVariable Long itemId,
            @Valid @RequestBody CartItemDTO cartItemDTO) {
        return ResponseEntity.ok(cartService.updateCartItem(itemId, cartItemDTO));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<String> removeItemFromCart(@PathVariable Long itemId) {
        cartService.removeItemFromCart(itemId);
        return ResponseEntity.ok("Đã xóa sản phẩm khỏi giỏ hàng với ID: " + itemId);
    }

    @DeleteMapping
    public ResponseEntity<String> clearCart() {
        cartService.clearCart();
        return ResponseEntity.ok("Đã xóa toàn bộ giỏ hàng.");
    }

}