package com.techstore.vanminh.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_transactions")
@Data
public class PaymentTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false, unique = true)
    private String transactionId;

    @Column(nullable = false)
    private String paymentMethod; // e.g., "MOMO"

    @Column(nullable = false)
    private String status; // e.g., PENDING, SUCCESS, FAILED

    private Double amount;

    private LocalDateTime createdAt;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String moMoResponse; // Store MoMo API response
}