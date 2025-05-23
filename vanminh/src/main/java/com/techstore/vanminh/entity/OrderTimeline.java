package com.techstore.vanminh.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_timeline")
@Data
@NoArgsConstructor
public class OrderTimeline {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonBackReference
    private Order order;

    @Enumerated(EnumType.STRING)
    private Order.OrderStatus status;

    private LocalDateTime date;

    private String description;
}