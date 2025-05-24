package com.techstore.vanminh.dto;

import lombok.Data;

import java.util.List;

import com.techstore.vanminh.dto.response.OrderResponseDTO;

@Data
public class UserDTO {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String avatarUrl;
    private List<AddressDTO> addresses;
    private CartDTO cart;
    private List<OrderResponseDTO> orders;
    private List<RoleDTO> roles;
}