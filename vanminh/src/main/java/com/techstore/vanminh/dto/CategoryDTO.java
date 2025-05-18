package com.techstore.vanminh.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class CategoryDTO {
    private Long id;

    @NotBlank(message = "Tên danh mục không được để trống")
    private String name;

    private String description;
}