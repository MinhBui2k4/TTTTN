package com.techstore.vanminh.dto;

import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class PaymentMethodDTO {
    private Long id;

    @NotBlank(message = "Tên phương thức thanh toán không được để trống")
    private String name;

    private String description;
}