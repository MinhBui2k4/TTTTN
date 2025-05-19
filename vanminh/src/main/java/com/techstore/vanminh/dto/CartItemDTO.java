package com.techstore.vanminh.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CartItemDTO {
    private Long id; // Không cần trong POST

    private Long cartId; // Không cần trong POST

    @NotNull(message = "Product ID không được để trống")
    private Long productId;

    private String productName; // Không cần trong POST

    private Double productPrice; // Không cần trong POST

    @NotNull(message = "Số lượng không được để trống")
    @Positive(message = "Số lượng phải lớn hơn 0")
    private Integer quantity;

    private Double totalPrice; // Không cần trong POST
}