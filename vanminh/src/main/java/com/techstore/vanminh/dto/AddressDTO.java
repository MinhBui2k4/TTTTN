package com.techstore.vanminh.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class AddressDTO {
    private Long id;

    @NotBlank(message = "Tên không được để trống")
    private String name;

    @NotBlank(message = "Số điện thoại không được để trống")
    private String phone;

    @NotBlank(message = "Địa chỉ không được để trống")
    private String address;

    private String ward;

    private String district;

    private String province;

    private boolean isDefault;

    private String type; // HOME, OFFICE
}