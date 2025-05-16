package com.techstore.vanminh.dto;

import lombok.Data;
import java.util.List;

@Data
public class ProductDTO {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private Double oldPrice;
    private Double rating;
    private Integer reviews;
    private String image;
    private List<String> images;
    private boolean isNew;
    private boolean isSale;
    private Integer maxQuantity;
    private String category;
    private String brand;
    private String sku;
    private String availability;
    private List<SpecificationDTO> specifications;
    private List<ProductVariantDTO> variants;

    @Data
    public static class SpecificationDTO {
        private String name;
        private String value;
    }
}