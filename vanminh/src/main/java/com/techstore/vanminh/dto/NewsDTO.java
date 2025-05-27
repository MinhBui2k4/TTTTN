package com.techstore.vanminh.dto;

import lombok.Data;
import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Data
public class NewsDTO {
    private Long id;

    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(max = 255, message = "Tiêu đề không được vượt quá 255 ký tự")
    private String title;

    @NotBlank(message = "Nội dung không được để trống")
    private String content;

    private String image;
    private MultipartFile imageFile; // File hình ảnh (cho POST/PUT)

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}