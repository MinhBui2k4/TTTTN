package com.techstore.vanminh.dto;

import lombok.Data;

@Data
public class CartItemDTO {
    private Long id;
    private Long cartId;
    private Long productId;
    private Integer quantity;
}