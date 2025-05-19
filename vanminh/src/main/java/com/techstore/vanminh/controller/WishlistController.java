package com.techstore.vanminh.controller;

import com.techstore.vanminh.dto.WishlistItemDTO;
import com.techstore.vanminh.dto.response.BaseResponse;
import com.techstore.vanminh.service.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users/wishlist")
@PreAuthorize("hasRole('ROLE_USER')")
public class WishlistController {

    @Autowired
    private WishlistService wishlistService;

    @PostMapping
    public ResponseEntity<BaseResponse<WishlistItemDTO>> addItemToWishlist(
            @Valid @RequestBody WishlistItemDTO wishlistItemDTO) {
        return ResponseEntity.ok(wishlistService.addItemToWishlist(wishlistItemDTO));
    }

    @GetMapping
    public ResponseEntity<BaseResponse<WishlistItemDTO>> getWishlist(
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(wishlistService.getWishlist(pageNumber, pageSize));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<BaseResponse<WishlistItemDTO>> removeItemFromWishlist(@PathVariable Long productId) {
        return ResponseEntity.ok(wishlistService.removeItemFromWishlist(productId));
    }

    @DeleteMapping
    public ResponseEntity<Void> clearWishlist() {
        wishlistService.clearWishlist();
        return ResponseEntity.noContent().build();
    }
}