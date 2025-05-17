package com.techstore.vanminh.service;

import com.techstore.vanminh.dto.RegisterDTO;
import com.techstore.vanminh.dto.UserDTO;
import com.techstore.vanminh.dto.response.UserResponse;

public interface UserService {

    UserDTO registerUser(RegisterDTO registerDTO);

    UserResponse getAllUsers(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);

    UserDTO getUserById(Long userId);

    UserDTO getUserByEmail(String email);

    UserDTO updateUser(Long userId, UserDTO userDTO);

    String deleteUser(Long userId);

}