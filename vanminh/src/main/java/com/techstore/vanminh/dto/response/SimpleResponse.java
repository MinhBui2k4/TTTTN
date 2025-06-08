package com.techstore.vanminh.dto.response;

import lombok.Data;

@Data
public class SimpleResponse<T> {
    private String message;
    private T content;
}