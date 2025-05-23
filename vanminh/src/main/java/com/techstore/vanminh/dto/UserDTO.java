package com.techstore.vanminh.dto;

import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.multipart.MultipartFile;


import java.util.List;

@Data
public class UserDTO {
    private Long id;

    @NotBlank(message = "Họ và tên không được để trống")
    private String fullName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;

    private String phone;

    private String avatarUrl;

    private MultipartFile avatarFile;

    private List<AddressDTO> addresses;

    private List<RoleDTO> roles;

    private CartDTO cart;

    private List<OrderDTO> orders; // Thêm danh sách đơn hàng
 
}