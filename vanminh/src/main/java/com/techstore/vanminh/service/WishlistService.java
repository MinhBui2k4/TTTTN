package com.techstore.vanminh.service;

import com.techstore.vanminh.dto.WishlistItemDTO;
import com.techstore.vanminh.dto.response.BaseResponse;

public interface WishlistService {
    BaseResponse<WishlistItemDTO> addItemToWishlist(WishlistItemDTO wishlistItemDTO);

    BaseResponse<WishlistItemDTO> getWishlist();

    BaseResponse<WishlistItemDTO> removeItemFromWishlist(Long productId);

    void clearWishlist();
}