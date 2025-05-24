package com.techstore.vanminh.service;

import java.io.FileNotFoundException;
import java.io.InputStream;

import com.techstore.vanminh.dto.RegisterDTO;
import com.techstore.vanminh.dto.UserDTO;
import com.techstore.vanminh.dto.response.BaseResponse;
import com.techstore.vanminh.dto.response.UserDTORequest;
import com.techstore.vanminh.dto.response.UserDTOResponse;

public interface UserService {

    UserDTO registerUser(RegisterDTO registerDTO);

    BaseResponse<UserDTO> getAllUsers(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);

    UserDTO getUserById(Long userId);

    UserDTO getUserById(Long userId, boolean includeOrders);

    UserDTO getUserByEmail(String email);

    public UserDTOResponse updateUser(Long userId, UserDTORequest userDTO);

    String deleteUser(Long userId);

    public UserDTO getProfile();

    public InputStream getAvatar(String fileName) throws FileNotFoundException;

}