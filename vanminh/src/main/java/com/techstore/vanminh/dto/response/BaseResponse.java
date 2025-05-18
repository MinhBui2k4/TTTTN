package com.techstore.vanminh.dto.response;

import java.util.List;

import lombok.Data;

@Data
public class BaseResponse<T> {
    private String message;
    private List<T> content;
    private Integer pageNumber;
    private Integer pageSize;
    private Long totalElements;
    private Integer totalPages;
    private boolean lastPage;
}
