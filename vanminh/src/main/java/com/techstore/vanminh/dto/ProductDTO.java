package com.techstore.vanminh.dto;

import lombok.Data;
import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class ProductDTO {
    private Long id;

    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(max = 255, message = "Tên sản phẩm không được vượt quá 255 ký tự")
    private String name;

    private String description;

    @NotNull(message = "Giá sản phẩm không được để trống")
    @Positive(message = "Giá sản phẩm phải lớn hơn 0")
    private Double price;

    @PositiveOrZero(message = "Giá cũ phải lớn hơn hoặc bằng 0")
    private Double oldPrice;

    @PositiveOrZero(message = "Điểm đánh giá phải lớn hơn hoặc bằng 0")
    @Max(value = 5, message = "Điểm đánh giá không được vượt quá 5")
    private Double rating;

    @PositiveOrZero(message = "Số lượng đánh giá phải lớn hơn hoặc bằng 0")
    private int review;

    private String image;
    private MultipartFile imageFile;

    private List<String> images;
    private List<MultipartFile> imageFiles;

    private boolean isNew;

    private boolean isSale;

    @PositiveOrZero(message = "Số lượng phải lớn hơn hoặc bằng 0")
    private Integer quantity;

    private Long categoryId;

    private Long brandId;

    private String sku;

    private boolean availability;
}