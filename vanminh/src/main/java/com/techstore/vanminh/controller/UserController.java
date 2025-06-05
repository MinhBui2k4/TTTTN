package com.techstore.vanminh.controller;

import com.techstore.vanminh.dto.AdminCreateUserDTO;
import com.techstore.vanminh.dto.ChangePasswordDTO;
import com.techstore.vanminh.dto.UserDTO;
import com.techstore.vanminh.dto.response.BaseResponse;
import com.techstore.vanminh.dto.response.UserDTORequest;
import com.techstore.vanminh.dto.response.UserDTOResponse;
import com.techstore.vanminh.entity.User;
import com.techstore.vanminh.exception.BadRequestException;
import com.techstore.vanminh.exception.ResourceNotFoundException;
import com.techstore.vanminh.repository.UserRepository;
import com.techstore.vanminh.service.UserService;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger log = Logger.getLogger(UserController.class.getName());

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    // Chỉ admin có thể lấy danh sách người dùng
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping
    public ResponseEntity<BaseResponse<UserDTO>> getAllUsers(
            @RequestParam(defaultValue = "0") Integer pageNumber,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortOrder) {
        return ResponseEntity.ok(userService.getAllUsers(pageNumber, pageSize, sortBy, sortOrder));
    }

    // Admin hoặc chính người dùng có thể lấy thông tin theo ID
    @PreAuthorize("hasRole('ROLE_ADMIN') or authentication.name == #email")
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean includeOrders) {
        UserDTO userDTO = userService.getUserById(id, includeOrders);
        return ResponseEntity.ok(userDTO);
    }

    // Admin hoặc chính người dùng có thể lấy thông tin theo email
    @PreAuthorize("hasRole('ROLE_ADMIN') or authentication.name == #email")
    @GetMapping("/email/{email}")
    public ResponseEntity<UserDTO> getUserByEmail(@PathVariable String email) {
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    @PutMapping(value = "", consumes = { "multipart/form-data" })
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or authentication.principal.id == #userDTO.id")
    public ResponseEntity<UserDTOResponse> updateUser(
            @Valid @ModelAttribute UserDTORequest userDTO) {

        if (userDTO.getId() == null) {
            throw new BadRequestException("ID người dùng không được để trống");
        }

        log.info("Received request to update user with ID: " + userDTO.getId());
        UserDTOResponse updatedUser = userService.updateUser(userDTO.getId(), userDTO);
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping(value = "/profile", consumes = { "multipart/form-data" })
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<UserDTOResponse> updateProfile(@Valid @ModelAttribute UserDTORequest userDTO) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Received request to update profile for email: " + email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tìm thấy với email: " + email));
        UserDTOResponse updatedUser = userService.updateUser(user.getId(), userDTO);
        return ResponseEntity.ok(updatedUser);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        try {
            String result = userService.deleteUser(id);
            return ResponseEntity.ok(result);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi xóa người dùng");
        }
    }

    // Người dùng đã đăng nhập có thể lấy profile của chính họ
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/profile")
    public ResponseEntity<UserDTO> getProfile() {
        // Lấy email từ người dùng đã đăng nhập
        String currentEmail = org.springframework.security.core.context.SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        UserDTO userDTO = userService.getUserByEmail(currentEmail);
        return ResponseEntity.ok(userDTO);
    }

    @GetMapping("/image/{fileName}")
    public ResponseEntity<InputStreamResource> getImage(@PathVariable String fileName) throws FileNotFoundException {
        InputStream imageStream = userService.getAvatar(fileName);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.setContentDispositionFormData("inline", fileName);

        return new ResponseEntity<>(new InputStreamResource(imageStream), headers, HttpStatus.OK);
    }

    // Xử lý exception trong controller
    @ExceptionHandler(MissingPathVariableException.class)
    public ResponseEntity<String> handleMissingPathVariable(MissingPathVariableException ex) {
        log.severe("Missing path variable: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Thiếu biến userId trong URL. Vui lòng cung cấp ID hợp lệ, ví dụ: /api/users/4");
    }

    @PostMapping("/change-password")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<String> changePassword(@Valid @RequestBody ChangePasswordDTO changePasswordDTO) {
        try {
            userService.changePassword(changePasswordDTO);
            return ResponseEntity.ok("Đổi mật khẩu thành công");
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.severe("Unexpected error while changing password: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi đổi mật khẩu");
        }
    }

      @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody AdminCreateUserDTO createUserDTO) {
        try {
            log.info("Received request to create user with email: " + createUserDTO.getEmail());
            UserDTO createdUser = userService.createUserByAdmin(createUserDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            log.severe("Unexpected error while creating user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

}