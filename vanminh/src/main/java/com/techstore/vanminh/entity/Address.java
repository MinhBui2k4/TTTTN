package com.techstore.vanminh.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "addresses")
@Data
@Getter
@Setter
@NoArgsConstructor 
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String address;

    private String ward;

    private String district;

    private String province;

    private boolean isDefault;

    @Enumerated(EnumType.STRING)
    private AddressType type;

    public enum AddressType {
        HOME, OFFICE
    }
}