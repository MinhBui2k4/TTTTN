package com.techstore.vanminh.dto;

import lombok.Data;
import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Data
public class HeroSectionDTO {
    private Long id;

    @NotBlank(message = "Tiêu đề không được để trống")
    private String heading;

    @NotBlank(message = "Phụ đề không được để trống")
    private String subheading;

    private String backgroundImage;
    private MultipartFile backgroundImageFile;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Long createdById;
}
