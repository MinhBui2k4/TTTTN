package com.techstore.vanminh.service;

import com.techstore.vanminh.dto.CartDTO;
import com.techstore.vanminh.dto.CartItemDTO;
import com.techstore.vanminh.dto.response.BaseResponse;

public interface CartService {
    BaseResponse<CartDTO> getCart();

    BaseResponse<CartDTO> addItemToCart(CartItemDTO cartItemDTO);

    BaseResponse<CartDTO> updateCartItem(Long itemId, CartItemDTO cartItemDTO);

    BaseResponse<CartDTO> removeItemFromCart(Long itemId);

    void clearCart();
}