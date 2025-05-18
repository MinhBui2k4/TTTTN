package com.techstore.vanminh.service;

import com.techstore.vanminh.dto.CartDTO;
import com.techstore.vanminh.dto.CartItemDTO;

public interface CartService {
    // BaseResponse<CartItemDTO> getCart(int pageNumber, int pageSize, String
    // sortBy, String sortOrder);
    CartDTO getCart();

    CartDTO addItemToCart(CartItemDTO cartItemDTO);

    CartDTO updateCartItem(Long itemId, CartItemDTO cartItemDTO);

    CartDTO removeItemFromCart(Long itemId);

    void clearCart();
}