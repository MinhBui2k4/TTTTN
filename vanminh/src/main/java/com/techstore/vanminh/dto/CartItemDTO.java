package com.techstore.vanminh.dto;

import lombok.Data;

@Data
public class CartItemDTO {
    private Long id;
    private Long cartId;
    private Long productId;
    private String productName; // Thêm tên sản phẩm
    private Double productPrice; // Thêm giá sản phẩm
    private Integer quantity;
    private Double totalPrice; // Tổng giá = productPrice * quantity
}