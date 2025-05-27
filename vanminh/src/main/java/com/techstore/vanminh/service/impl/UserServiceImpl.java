package com.techstore.vanminh.service.impl;

import com.techstore.vanminh.dto.*;
import com.techstore.vanminh.dto.response.BaseResponse;
import com.techstore.vanminh.dto.response.UserDTORequest;
import com.techstore.vanminh.dto.response.UserDTOResponse;
import com.techstore.vanminh.entity.*;
import com.techstore.vanminh.exception.*;
import com.techstore.vanminh.repository.*;
import com.techstore.vanminh.service.*;
import com.techstore.vanminh.util.CartMapper;
import com.techstore.vanminh.util.OrderMapper;
import org.springframework.security.core.context.SecurityContextHolder;
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
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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
    private WishlistRepository wishlistRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private FileService fileService;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private ModelMapper modelMapper;

    @Value("${project.avatar.path}")
    private String avatarPath;

    // Helper method to map Wishlist to WishlistDTO
    private WishlistDTO mapToWishlistDTO(Wishlist wishlist) {
        if (wishlist == null) {
            return null;
        }
        WishlistDTO wishlistDTO = new WishlistDTO();
        wishlistDTO.setId(wishlist.getId());
        wishlistDTO.setUserId(wishlist.getUser() != null ? wishlist.getUser().getId() : null);
        wishlistDTO.setItems(wishlist.getItems().stream()
                .map(this::mapToWishlistItemDTO)
                .collect(Collectors.toList()));
        return wishlistDTO;
    }

    // Helper method to map WishlistItem to WishlistItemDTO
    private WishlistItemDTO mapToWishlistItemDTO(WishlistItem item) {
        WishlistItemDTO itemDTO = new WishlistItemDTO();
        itemDTO.setId(item.getId());
        itemDTO.setWishlistId(item.getWishlist() != null ? item.getWishlist().getId() : null);
        itemDTO.setProductId(item.getProduct() != null ? item.getProduct().getId() : null);
        itemDTO.setProductName(item.getProduct() != null ? item.getProduct().getName() : null);
        itemDTO.setProductPrice(item.getProduct() != null ? item.getProduct().getPrice() : null);
        itemDTO.setAvailability(item.getProduct() != null ? item.getProduct().isAvailability() : false);
        return itemDTO;
    }

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

        String roleName = registerDTO.getRoleName() != null ? registerDTO.getRoleName().toUpperCase() : "USER";
        try {
            Role.RoleName roleEnum = Role.RoleName.valueOf(roleName);
            Role userRole = roleRepository.findByName(roleEnum)
                    .orElseThrow(() -> new BadRequestException("Role not found!"));
            user.setRoles(Collections.singletonList(userRole));
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid role name!");
        }

        User registeredUser = userRepository.save(user);

        UserDTO userDTO = new UserDTO();
        userDTO.setId(registeredUser.getId());
        userDTO.setFullName(registeredUser.getFullName());
        userDTO.setEmail(registeredUser.getEmail());
        userDTO.setPhone(registeredUser.getPhone());
        userDTO.setAvatarUrl(registeredUser.getAvatarUrl());
        return userDTO;
    }

    @Override
    @Transactional
    public UserDTOResponse updateUser(Long userId, UserDTORequest userDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tìm thấy với id: " + userId));

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

        UserDTOResponse response = new UserDTOResponse();
        response.setId(updatedUser.getId());
        response.setFullName(updatedUser.getFullName());
        response.setEmail(updatedUser.getEmail());
        response.setPhone(updatedUser.getPhone());
        response.setAvatarUrl(updatedUser.getAvatarUrl());

        if (updatedUser.getAddresses() != null) {
            response.setAddresses(updatedUser.getAddresses().stream()
                    .map(address -> {
                        AddressDTO addressDTO = new AddressDTO();
                        addressDTO.setId(address.getId());
                        addressDTO.setName(address.getName());
                        addressDTO.setPhone(address.getPhone());
                        addressDTO.setAddress(address.getAddress());
                        addressDTO.setWard(address.getWard());
                        addressDTO.setDistrict(address.getDistrict());
                        addressDTO.setProvince(address.getProvince());
                        addressDTO.setType(address.getType());
                        addressDTO.setDefault(address.isDefault());
                        return addressDTO;
                    })
                    .collect(Collectors.toList()));
        }

        if (updatedUser.getRoles() != null) {
            response.setRoles(updatedUser.getRoles().stream()
                    .map(role -> {
                        RoleDTO roleDTO = new RoleDTO();
                        roleDTO.setId(role.getId());
                        roleDTO.setName(role.getName() != null ? role.getName().name() : null);
                        return roleDTO;
                    })
                    .collect(Collectors.toList()));
        }

        if (updatedUser.getCart() != null) {
            response.setCart(cartMapper.mapToCartDTO(updatedUser.getCart()));
        }

        // Map Wishlist
        Wishlist wishlist = wishlistRepository.findByUserId(updatedUser.getId()).orElse(null);
        response.setWishlist(mapToWishlistDTO(wishlist));

        return response;
    }

    @Override
    public UserDTO getUserById(Long userId, boolean includeOrders) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy User với id: " + userId));

        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setFullName(user.getFullName());
        userDTO.setEmail(user.getEmail());
        userDTO.setPhone(user.getPhone());
        userDTO.setAvatarUrl(user.getAvatarUrl());

        if (user.getAddresses() != null) {
            userDTO.setAddresses(user.getAddresses().stream()
                    .map(address -> {
                        AddressDTO addressDTO = new AddressDTO();
                        addressDTO.setId(address.getId());
                        addressDTO.setName(address.getName());
                        addressDTO.setPhone(address.getPhone());
                        addressDTO.setAddress(address.getAddress());
                        addressDTO.setWard(address.getWard());
                        addressDTO.setDistrict(address.getDistrict());
                        addressDTO.setProvince(address.getProvince());
                        addressDTO.setType(address.getType());
                        addressDTO.setDefault(address.isDefault());
                        return addressDTO;
                    })
                    .collect(Collectors.toList()));
        }

        if (user.getCart() != null) {
            userDTO.setCart(cartMapper.mapToCartDTO(user.getCart()));
        }

        if (includeOrders && user.getOrders() != null) {
            userDTO.setOrders(user.getOrders().stream()
                    .sorted((o1, o2) -> o2.getOrderDate().compareTo(o1.getOrderDate()))
                    .limit(10)
                    .map(orderMapper::toOrderResponseDTO)
                    .collect(Collectors.toList()));
        }

        // Map Wishlist
        userDTO.setWishlist(mapToWishlistDTO(user.getWishlist()));

        return userDTO;
    }

    @Override
    public UserDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setFullName(user.getFullName());
        userDTO.setEmail(user.getEmail());
        userDTO.setPhone(user.getPhone());
        userDTO.setAvatarUrl(user.getAvatarUrl());

        if (user.getAddresses() != null) {
            userDTO.setAddresses(user.getAddresses().stream()
                    .map(address -> {
                        AddressDTO addressDTO = new AddressDTO();
                        addressDTO.setId(address.getId());
                        addressDTO.setName(address.getName());
                        addressDTO.setPhone(address.getPhone());
                        addressDTO.setAddress(address.getAddress());
                        addressDTO.setWard(address.getWard());
                        addressDTO.setDistrict(address.getDistrict());
                        addressDTO.setProvince(address.getProvince());
                        addressDTO.setType(address.getType());
                        addressDTO.setDefault(address.isDefault());
                        return addressDTO;
                    })
                    .collect(Collectors.toList()));
        }

        if (user.getCart() != null) {
            userDTO.setCart(cartMapper.mapToCartDTO(user.getCart()));
        }

        if (user.getOrders() != null) {
            userDTO.setOrders(user.getOrders().stream()
                    .map(orderMapper::toOrderResponseDTO)
                    .collect(Collectors.toList()));
        }

        // Map Wishlist
        Wishlist wishlist = wishlistRepository.findByUserId(user.getId()).orElse(null);
        userDTO.setWishlist(mapToWishlistDTO(wishlist));

        return userDTO;
    }

    @Override
    public UserDTO getUserById(Long userId) {
        return getUserById(userId, false);
    }

    @Override
    public UserDTO getProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return getUserByEmail(email);
    }

    @Override
    @Transactional
    public String deleteUser(Long userId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tìm thấy với id: " + userId));
            log.info("Attempting to delete user with ID: " + userId);

            long orderCount = orderRepository.countByUserId(userId);
            if (orderCount > 0) {
                log.warning("Cannot delete user with ID: " + userId + " due to existing orders: " + orderCount);
                throw new BadRequestException(
                        "Không thể xóa người dùng vì họ có " + orderCount + " đơn hàng liên quan");
            }

            user.getRoles().clear();
            userRepository.save(user);

            userRepository.delete(user);
            log.info("Successfully deleted user with ID: " + userId);
            return "Đã xóa người dùng với ID: " + userId;
        } catch (DataIntegrityViolationException e) {
            log.severe("Failed to delete user due to database constraint: " + e.getMessage());
            throw new BadRequestException("Không thể xóa người dùng do có dữ liệu liên quan");
        } catch (Exception e) {
            log.severe("Unexpected error while deleting user: " + e.getMessage());
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
                    UserDTO dto = new UserDTO();
                    dto.setId(user.getId());
                    dto.setFullName(user.getFullName());
                    dto.setEmail(user.getEmail());
                    dto.setPhone(user.getPhone());
                    dto.setAvatarUrl(user.getAvatarUrl());

                    if (user.getAddresses() != null) {
                        dto.setAddresses(user.getAddresses().stream()
                                .map(address -> {
                                    AddressDTO addressDTO = new AddressDTO();
                                    addressDTO.setId(address.getId());
                                    addressDTO.setName(address.getName());
                                    addressDTO.setPhone(address.getPhone());
                                    addressDTO.setAddress(address.getAddress());
                                    addressDTO.setWard(address.getWard());
                                    addressDTO.setDistrict(address.getDistrict());
                                    addressDTO.setProvince(address.getProvince());
                                    addressDTO.setType(address.getType());
                                    addressDTO.setDefault(address.isDefault());
                                    return addressDTO;
                                })
                                .collect(Collectors.toList()));
                    }

                    if (user.getCart() != null) {
                        dto.setCart(cartMapper.mapToCartDTO(user.getCart()));
                    }

                    if (user.getOrders() != null) {
                        dto.setOrders(user.getOrders().stream()
                                .map(orderMapper::toOrderResponseDTO)
                                .collect(Collectors.toList()));
                    }

                    // Fetch and map Wishlist
                    Wishlist wishlist = wishlistRepository.findByUserId(user.getId()).orElse(null);
                    dto.setWishlist(mapToWishlistDTO(wishlist));

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