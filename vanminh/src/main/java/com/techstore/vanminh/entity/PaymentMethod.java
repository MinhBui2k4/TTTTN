package com.techstore.vanminh.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "payment_methods")
@Data
@NoArgsConstructor
public class PaymentMethod {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // Ví dụ: "COD", "Credit Card"

    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "paymentMethod", cascade = CascadeType.ALL)
    private List<Order> orders;
}