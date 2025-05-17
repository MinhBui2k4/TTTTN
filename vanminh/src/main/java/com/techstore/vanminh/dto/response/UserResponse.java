package com.techstore.vanminh.dto.response;

import lombok.Data;

import java.util.List;

import com.techstore.vanminh.dto.UserDTO;

@Data
public class UserResponse {
    private List<UserDTO> content;
    private Integer pageNumber;
    private Integer pageSize;
    private Long totalElements;
    private Integer totalPages;
    private boolean lastPage;
}