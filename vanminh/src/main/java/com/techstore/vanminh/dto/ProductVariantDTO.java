package com.techstore.vanminh.dto;

import lombok.Data;
import java.util.List;

@Data
public class ProductVariantDTO {
    private Long id;
    private Long productId;
    private String name;
    private List<String> options;
}