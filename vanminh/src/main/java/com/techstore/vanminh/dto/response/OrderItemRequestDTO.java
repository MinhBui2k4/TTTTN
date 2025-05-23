package com.techstore.vanminh.dto.response;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class OrderItemRequestDTO {
    @NotNull(message = "Product ID is required")
    @Positive(message = "Product ID must be positive")
    private Long productId;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private Integer quantity;
}