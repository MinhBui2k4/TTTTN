package com.techstore.vanminh.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDTO {
    private Long id;
    private Long userId;
    private LocalDateTime orderDate;
    private String status;
    private Double total;
    private Double shippingCost;
    private Long paymentMethodId;
    private Long shippingAddressId;
    private List<OrderItemDTO> items;
    private List<OrderTimelineDTO> timeline;
}