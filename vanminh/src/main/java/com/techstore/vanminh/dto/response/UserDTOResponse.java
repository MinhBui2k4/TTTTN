package com.techstore.vanminh.dto.response;

import lombok.Data;

import java.util.List;

import com.techstore.vanminh.dto.AddressDTO;
import com.techstore.vanminh.dto.CartDTO;
import com.techstore.vanminh.dto.RoleDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
public class UserDTOResponse {
    private Long id;

    @NotBlank(message = "Họ và tên không được để trống")
    @Size(max = 255, message = "Họ và tên không được vượt quá 255 ký tự")
    private String fullName;

    @NotBlank(message = "Email không được để trống")
    private String email;

    @Size(max = 20, message = "Số điện thoại không được vượt quá 20 ký tự")
    private String phone;

    private String avatarUrl;

    private List<AddressDTO> addresses;
    private List<RoleDTO> roles;
    private CartDTO cart;
}