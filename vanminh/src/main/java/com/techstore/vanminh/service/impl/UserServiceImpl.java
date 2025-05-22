package com.techstore.vanminh.service.impl;

import com.techstore.vanminh.dto.AddressDTO;
import com.techstore.vanminh.dto.RegisterDTO;
import com.techstore.vanminh.dto.RoleDTO;
import com.techstore.vanminh.dto.UserDTO;
import com.techstore.vanminh.dto.response.BaseResponse;
import com.techstore.vanminh.dto.response.UserDTORequest;
import com.techstore.vanminh.dto.response.UserDTOResponse;
import com.techstore.vanminh.entity.Address;
import com.techstore.vanminh.entity.Cart;
import com.techstore.vanminh.entity.Order;
import com.techstore.vanminh.entity.Role;
import com.techstore.vanminh.entity.User;
import com.techstore.vanminh.exception.BadRequestException;
import com.techstore.vanminh.exception.ResourceNotFoundException;
import com.techstore.vanminh.repository.OrderRepository;
import com.techstore.vanminh.repository.RoleRepository;
import com.techstore.vanminh.repository.UserRepository;
import com.techstore.vanminh.service.FileService;
import com.techstore.vanminh.service.UserService;
import com.techstore.vanminh.util.CartMapper;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.logging.Logger;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private static final Logger log = Logger.getLogger(UserServiceImpl.class.getName());

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private FileService fileService;

    @Autowired
    private CartMapper cartMapper;

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
    @Transactional
    public UserDTOResponse updateUser(Long userId, UserDTORequest userDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tìm thấy với id: " + userId));

        // Kiểm tra email trùng lặp
        if (!user.getEmail().equals(userDTO.getEmail()) && userRepository.findByEmail(userDTO.getEmail()).isPresent()) {
            throw new BadRequestException("Email đã được sử dụng");
        }

        user.setFullName(userDTO.getFullName());
        user.setEmail(userDTO.getEmail());
        user.setPhone(userDTO.getPhone());

        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }

        if (userDTO.getAvatarFile() != null && !userDTO.getAvatarFile().isEmpty()) {
            try {
                String fileName = fileService.uploadAvatar(avatarPath, userDTO.getAvatarFile(), userId);
                user.setAvatarUrl(fileName);
            } catch (IOException e) {
                throw new BadRequestException("Không thể tải lên avatar: " + e.getMessage());
            }
        }

        User updatedUser = userRepository.save(user);
        UserDTOResponse response = modelMapper.map(updatedUser, UserDTOResponse.class);

        if (updatedUser.getAddresses() != null) {
            response.setAddresses(updatedUser.getAddresses().stream()
                    .map(address -> modelMapper.map(address, AddressDTO.class))
                    .collect(Collectors.toList()));
        }

        if (updatedUser.getRoles() != null) {
            response.setRoles(updatedUser.getRoles().stream()
                    .map(role -> modelMapper.map(role, RoleDTO.class))
                    .collect(Collectors.toList()));
        }

        if (updatedUser.getCart() != null) {
            response.setCart(cartMapper.mapToCartDTO(updatedUser.getCart()));
        }

        return response;
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
            userDTO.setCart(cartMapper.mapToCartDTO(user.getCart()));
        }

        return userDTO;
    }

    @Override
    public UserDTO getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy User với id: " + userId));

        UserDTO userDTO = modelMapper.map(user, UserDTO.class);

        if (user.getAddresses() != null) {
            userDTO.setAddresses(user.getAddresses().stream()
                    .map(address -> modelMapper.map(address, AddressDTO.class))
                    .collect(Collectors.toList()));
        }

        if (user.getCart() != null) {
            userDTO.setCart(cartMapper.mapToCartDTO(user.getCart()));
        }

        return userDTO;
    }

    @Override
    @Transactional
    public String deleteUser(Long userId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tìm thấy với id: " + userId));
            log.info("Attempting to delete user with ID: " + userId);

            // Kiểm tra xem user có Order nào không
            long orderCount = orderRepository.countByUserId(userId);
            if (orderCount > 0) {
                log.warning("Cannot delete user with ID: " + userId + " due to existing orders: " + orderCount);
                throw new BadRequestException(
                        "Không thể xóa người dùng vì họ có " + orderCount + " đơn hàng liên quan");
            }

            // Xóa quan hệ với roles
            user.getRoles().clear();
            userRepository.save(user);

            userRepository.delete(user);
            log.info("Successfully deleted user with ID: {}" + userId);
            return "Đã xóa người dùng với ID: " + userId;
        } catch (DataIntegrityViolationException e) {
            log.severe("Failed to delete user due to database constraint: {}" + e.getMessage());
            throw new BadRequestException("Không thể xóa người dùng do có dữ liệu liên quan");
        } catch (Exception e) {
            log.severe("Unexpected error while deleting user: {}" + e.getMessage());
            throw new RuntimeException("Lỗi khi xóa người dùng: " + e.getMessage());
        }
    }

    @Override
    public BaseResponse<UserDTO> getAllUsers(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
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
                        dto.setCart(cartMapper.mapToCartDTO(user.getCart()));
                    }
                    return dto;
                })
                .collect(Collectors.toList());

        BaseResponse<UserDTO> response = new BaseResponse<>();
        response.setContent(userDTOs);
        response.setPageNumber(userPage.getNumber());
        response.setPageSize(userPage.getSize());
        response.setTotalElements(userPage.getTotalElements());
        response.setTotalPages(userPage.getTotalPages());
        response.setLastPage(userPage.isLast());

        return response;
    }

    @Override
    public InputStream getAvatar(String fileName) throws FileNotFoundException {
        return fileService.getResource(avatarPath, fileName);
    }
}