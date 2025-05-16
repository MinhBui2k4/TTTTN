package com.techstore.vanminh.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class UserDTO {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private LocalDate birthday;
    private String gender;
    private String avatarUrl;
    private List<AddressDTO> addresses;
    private String defaultAddress;
    private List<RoleDTO> roles;
}