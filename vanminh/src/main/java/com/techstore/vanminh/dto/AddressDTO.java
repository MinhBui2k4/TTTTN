package com.techstore.vanminh.dto;

import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class AddressDTO {
    private Long id;

    private Long userId;

    @NotBlank(message = "Tên người nhận không được để trống")
    private String name;

    @NotBlank(message = "Số điện thoại không được để trống")
    private String phone;

    @NotBlank(message = "Địa chỉ không được để trống")
    private String address;

    private String ward;

    private String district;

    private String province;

    private String type; // Ví dụ: "HOME", "OFFICE"

    private boolean isDefault;
}