package com.techstore.vanminh.service;

import java.util.Collections;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.techstore.vanminh.dto.UserDTO;
import com.techstore.vanminh.entity.Role;
import com.techstore.vanminh.entity.User;
import com.techstore.vanminh.exception.ResourceNotFoundException;
import com.techstore.vanminh.repository.RoleRepository;
import com.techstore.vanminh.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;


@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserDTO createUser(UserDTO userDTO) {
        User user = modelMapper.map(userDTO, User.class);
        user.setPassword(passwordEncoder.encode(user.getPassword())); // Mã hóa mật khẩu

        // Gán vai trò mặc định (USER)
        Role userRole = roleRepository.findByName(Role.RoleName.USER)
                .orElseThrow(() -> new ResourceNotFoundException("Role USER not found"));
        user.setRoles(Collections.singletonList(userRole));

        user = userRepository.save(user);
        return modelMapper.map(user, UserDTO.class);
    }

    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return modelMapper.map(user, UserDTO.class);
    }
}