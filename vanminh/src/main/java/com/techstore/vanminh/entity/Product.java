package com.techstore.vanminh.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "products")
@Data

@NoArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Double price;

    private Double oldPrice;

    private Double rating;

    private Integer reviews;

    private String image;

    @ElementCollection
    private List<String> images;

    private boolean isNew;

    private boolean isSale;

    private Integer maxQuantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category; // Thay trường category (String) bằng Category entity

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand; // Thay trường brand (String) bằng Brand entity

    private String sku;

    private String availability;

    @ElementCollection
    @CollectionTable(name = "product_specifications")
    private List<Specification> specifications;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductVariant> variants;

    @Embeddable
    public static class Specification {
        private String name;
        private String value;
    }
}