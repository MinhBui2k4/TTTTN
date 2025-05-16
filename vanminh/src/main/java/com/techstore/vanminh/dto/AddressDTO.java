package com.techstore.vanminh.dto;

import lombok.Data;

@Data
public class AddressDTO {
    private Long id;
    private Long userId;
    private String name;
    private String phone;
    private String address;
    private String ward;
    private String district;
    private String province;
    private boolean isDefault;
    private String type;
}