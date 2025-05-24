package com.techstore.vanminh.dto.response;

import com.techstore.vanminh.dto.AddressDTO;
import com.techstore.vanminh.dto.CartDTO;
import com.techstore.vanminh.dto.RoleDTO;
import lombok.Data;

import java.util.List;

@Data
public class UserDTOResponse {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String avatarUrl;
    private List<AddressDTO> addresses;
    private List<RoleDTO> roles;
    private CartDTO cart;
    private List<OrderResponseDTO> orders;
}