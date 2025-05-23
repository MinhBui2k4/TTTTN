package com.techstore.vanminh.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponseDTO {
    private Long id;
    private Long userId;
    private LocalDateTime orderDate;
    private String status;
    private Double total;
    private Double shippingCost;
    private Long paymentMethodId;
    private Long shippingAddressId;
    private List<OrderItemResponseDTO> items;
    private List<OrderTimelineResponseDTO> timeline;
}