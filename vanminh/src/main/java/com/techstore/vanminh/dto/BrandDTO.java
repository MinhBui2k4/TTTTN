package com.techstore.vanminh.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class BrandDTO {
    private Long id;

    @NotBlank(message = "Tên thương hiệu không được để trống")
    private String name;

    private String description;
}
