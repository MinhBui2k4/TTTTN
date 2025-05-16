package com.techstore.vanminh.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "products")
@Data
@Getter
@Setter
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

    private String category;

    private String brand;

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