package com.techstore.vanminh.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class OrderTimelineDTO {
    private Long id;
    private Long orderId;
    private String status;
    private LocalDateTime date;
    private String description;
}