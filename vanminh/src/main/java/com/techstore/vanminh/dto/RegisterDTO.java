package com.techstore.vanminh.dto;

import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Data
public class RegisterDTO {
    @NotBlank(message = "Họ và tên không được để trống")
    private String fullName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;

    private String phone;

    private String roleName; // Ví dụ: "USER", "ADMIN"

    // private MultipartFile avatarFile; // Thêm để hỗ trợ upload avatar
}