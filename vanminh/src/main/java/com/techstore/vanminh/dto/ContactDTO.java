package com.techstore.vanminh.dto;

import lombok.Data;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

@Data
public class ContactDTO {
    private Long id;

    @NotBlank(message = "Tên không được để trống")
    private String name;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    private String phone;

    @NotBlank(message = "Nội dung không được để trống")
    private String message;

    private String status; // ContactStatus as String

    private LocalDateTime createdAt;

    private Long userId; // Có thể null
}