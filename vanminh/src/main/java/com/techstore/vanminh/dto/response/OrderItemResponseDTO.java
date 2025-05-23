package com.techstore.vanminh.dto.response;

import lombok.Data;

@Data
public class OrderItemResponseDTO {
    private Long id;
    private Long productId;
    private String productName;
    private Integer quantity;
    private Double price;
}