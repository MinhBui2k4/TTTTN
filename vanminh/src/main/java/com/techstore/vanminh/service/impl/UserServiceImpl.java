package com.techstore.vanminh.service.impl;

import com.techstore.vanminh.dto.AddressDTO;
import com.techstore.vanminh.dto.CartDTO;
import com.techstore.vanminh.dto.RegisterDTO;
import com.techstore.vanminh.dto.UserDTO;
import com.techstore.vanminh.dto.response.UserResponse;
import com.techstore.vanminh.entity.Address;
import com.techstore.vanminh.entity.Cart;
import com.techstore.vanminh.entity.Role;
import com.techstore.vanminh.entity.User;
import com.techstore.vanminh.exception.BadRequestException;
import com.techstore.vanminh.exception.ResourceNotFoundException;
import com.techstore.vanminh.repository.RoleRepository;
import com.techstore.vanminh.repository.UserRepository;
import com.techstore.vanminh.service.FileService;
import com.techstore.vanminh.service.UserService;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private FileService fileService;

    @Value("${project.avatar.path}")
    private String avatarPath;

    @Override
    public UserDTO registerUser(RegisterDTO registerDTO) {
        if (userRepository.findByEmail(registerDTO.getEmail()).isPresent()) {
            throw new BadRequestException("Email already exists!");
        }

        User user = new User();
        user.setFullName(registerDTO.getFullName());
        user.setEmail(registerDTO.getEmail());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setPhone(registerDTO.getPhone());

        // Set role based on roleName
        String roleName = registerDTO.getRoleName() != null ? registerDTO.getRoleName().toUpperCase() : "USER";
        try {
            Role.RoleName roleEnum = Role.RoleName.valueOf(roleName);
            Role userRole = roleRepository.findByName(roleEnum)
                    .orElseThrow(() -> new BadRequestException("Role not found!"));
            user.setRoles(Collections.singletonList(userRole));
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid role name!");
        }

        // Save user to get ID for file naming
        User registeredUser = userRepository.save(user);

        // Map back to DTO
        return modelMapper.map(registeredUser, UserDTO.class);
    }

    @Override
    public UserDTO updateUser(Long userId, UserDTO userDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setFullName(userDTO.getFullName());
        user.setEmail(userDTO.getEmail());
        user.setPhone(userDTO.getPhone());

        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }

        if (userDTO.getAddresses() != null) {
            user.setAddresses(userDTO.getAddresses().stream()
                    .map(dto -> modelMapper.map(dto, Address.class))
                    .collect(Collectors.toList()));
        }

        if (userDTO.getCart() != null) {
            Cart cart = modelMapper.map(userDTO.getCart(), Cart.class);
            cart.setUser(user);
            user.setCart(cart);
        }

        // Handle avatar upload
        if (userDTO.getAvatarFile() != null && !userDTO.getAvatarFile().isEmpty()) {
            try {
                String fileName = fileService.uploadAvatar(avatarPath, userDTO.getAvatarFile(), userId);
                user.setAvatarUrl(fileName);
            } catch (IOException e) {
                throw new BadRequestException("Failed to upload avatar: " + e.getMessage());
            }
        }

        User updatedUser = userRepository.save(user);
        UserDTO updatedUserDTO = modelMapper.map(updatedUser, UserDTO.class);

        if (updatedUser.getAddresses() != null) {
            updatedUserDTO.setAddresses(updatedUser.getAddresses().stream()
                    .map(address -> modelMapper.map(address, AddressDTO.class))
                    .collect(Collectors.toList()));
        }
        if (updatedUser.getCart() != null) {
            updatedUserDTO.setCart(modelMapper.map(updatedUser.getCart(), CartDTO.class));
        }

        return updatedUserDTO;
    }

    @Override
    public UserDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        UserDTO userDTO = modelMapper.map(user, UserDTO.class);

        if (user.getAddresses() != null) {
            userDTO.setAddresses(user.getAddresses().stream()
                    .map(address -> modelMapper.map(address, AddressDTO.class))
                    .collect(Collectors.toList()));
        }

        if (user.getCart() != null) {
            userDTO.setCart(modelMapper.map(user.getCart(), CartDTO.class));
        }

        return userDTO;
    }

    @Override
    public UserDTO getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        UserDTO userDTO = modelMapper.map(user, UserDTO.class);

        if (user.getAddresses() != null) {
            userDTO.setAddresses(user.getAddresses().stream()
                    .map(address -> modelMapper.map(address, AddressDTO.class))
                    .collect(Collectors.toList()));
        }

        if (user.getCart() != null) {
            userDTO.setCart(modelMapper.map(user.getCart(), CartDTO.class));
        }

        return userDTO;
    }

    @Override
    public String deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        userRepository.delete(user);
        return "User deleted successfully with id: " + userId;
    }

    @Override
    public UserResponse getAllUsers(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sort = sortOrder.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        Page<User> userPage = userRepository.findAll(pageable);

        List<UserDTO> userDTOs = userPage.getContent().stream()
                .map(user -> {
                    UserDTO dto = modelMapper.map(user, UserDTO.class);
                    if (user.getAddresses() != null) {
                        dto.setAddresses(user.getAddresses().stream()
                                .map(address -> modelMapper.map(address, AddressDTO.class))
                                .collect(Collectors.toList()));
                    }
                    if (user.getCart() != null) {
                        dto.setCart(modelMapper.map(user.getCart(), CartDTO.class));
                    }
                    return dto;
                })
                .collect(Collectors.toList());

        UserResponse response = new UserResponse();
        response.setContent(userDTOs);
        response.setPageNumber(userPage.getNumber());
        response.setPageSize(userPage.getSize());
        response.setTotalElements(userPage.getTotalElements());
        response.setTotalPages(userPage.getTotalPages());
        response.setLastPage(userPage.isLast());

        return response;
    }
}