package com.techstore.vanminh.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class OrderTimelineDTO {
    private Long id;
    private Long orderId;
    private String status; // Order.OrderStatus as String
    private LocalDateTime date;
    private String description;
}