package com.techstore.vanminh.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderTimelineResponseDTO {
    private Long id;
    private String status;
    private LocalDateTime date;
    private String description;
}