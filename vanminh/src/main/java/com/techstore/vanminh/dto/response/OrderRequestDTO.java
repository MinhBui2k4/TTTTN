package com.techstore.vanminh.dto.response;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class OrderRequestDTO {
    @NotNull(message = "User ID is required")
    @Positive(message = "User ID must be positive")
    private Long userId;

    @NotNull(message = "Shipping cost is required")
    @PositiveOrZero(message = "Shipping cost must be positive or zero")
    private Double shippingCost;

    @NotNull(message = "Payment method ID is required")
    @Positive(message = "Payment method ID must be positive")
    private Long paymentMethodId;

    @NotNull(message = "Shipping address ID is required")
    @Positive(message = "Shipping address ID must be positive")
    private Long shippingAddressId;

    @NotEmpty(message = "Order must contain at least one item")
    private List<OrderItemRequestDTO> items;
}