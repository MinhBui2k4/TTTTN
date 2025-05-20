package com.techstore.vanminh.controller;

import com.techstore.vanminh.dto.UserDTO;
import com.techstore.vanminh.dto.response.BaseResponse;
import com.techstore.vanminh.service.UserService;

import java.io.FileNotFoundException;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

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
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        UserDTO userDTO = userService.getUserById(id);
        return ResponseEntity.ok(userDTO);
    }

    // Admin hoặc chính người dùng có thể lấy thông tin theo email
    @PreAuthorize("hasRole('ROLE_ADMIN') or authentication.name == #email")
    @GetMapping("/email/{email}")
    public ResponseEntity<UserDTO> getUserByEmail(@PathVariable String email) {
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    // Admin hoặc chính người dùng có thể cập nhật thông tin
    @PreAuthorize("hasRole('ROLE_ADMIN') or authentication.name == #userDTO.email")
    @PutMapping(value = "/{id}", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @Valid @ModelAttribute UserDTO userDTO) {
        return ResponseEntity.ok(userService.updateUser(id, userDTO));
    }

    // Chỉ admin có thể xóa người dùng
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        return ResponseEntity.ok("Đã xóa người dùng với ID: " + id);
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
}