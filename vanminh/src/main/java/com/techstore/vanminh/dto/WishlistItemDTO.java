package com.techstore.vanminh.dto;

import lombok.Data;

@Data
public class WishlistItemDTO {
    private Long id;
    private Long wishlistId;
    private Long productId;
    private String productName;
    private Double productPrice;
    private Boolean availability;
}