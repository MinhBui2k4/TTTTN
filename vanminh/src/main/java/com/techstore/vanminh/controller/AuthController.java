package com.techstore.vanminh.controller;

import com.techstore.vanminh.dto.RegisterDTO;
import com.techstore.vanminh.dto.RoleDTO;
import com.techstore.vanminh.dto.UserDTO;
import com.techstore.vanminh.exception.BadRequestException;
import com.techstore.vanminh.security.JwtUtil;
import com.techstore.vanminh.service.UserService;

import io.jsonwebtoken.security.WeakKeyException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    // Đăng ký người dùng
    @PostMapping(value = "/register", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<?> register(@Valid @ModelAttribute RegisterDTO registerDTO) {
        try {
            UserDTO createdUser = userService.registerUser(registerDTO);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Đăng ký thành công!");
            response.put("user", createdUser);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Đăng ký thất bại: " + e.getMessage());
        }
    }

    // Đăng nhập
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest loginRequest) {
        try {
            // Xác thực người dùng
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Lấy thông tin người dùng
            UserDTO userDTO = userService.getUserByEmail(loginRequest.getEmail());
            if (userDTO == null) {
                throw new BadRequestException(
                        "Không tìm thấy dữ liệu người dùng cho email: " + loginRequest.getEmail());
            }

            // Tạo JWT token
            // String token = jwtUtil.generateToken(loginRequest.getEmail());

            // Lấy danh sách vai trò
            List<String> roles = userDTO.getRoles() != null
                    ? userDTO.getRoles().stream().map(RoleDTO::getName).toList()
                    : List.of();

            // Tạo JWT token (có chứa roles)
            String token = jwtUtil.generateToken(loginRequest.getEmail(), roles);

            // Tạo phản hồi
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Đăng nhập thành công!");
            response.put("token", token);
            response.put("email", userDTO.getEmail());
            response.put("userId", userDTO.getId().toString());
            response.put("roles", roles);

            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            throw new BadRequestException("Sai email hoặc mật khẩu");
        } catch (AuthenticationException e) {
            throw new BadRequestException("Xác thực thất bại: " + e.getMessage());
        } catch (WeakKeyException e) {
            throw new BadRequestException("Lỗi cấu hình khóa JWT: " + e.getMessage());
        }
    }

    // DTO cho yêu cầu đăng nhập
    public static class LoginRequest {
        private String email;
        private String password;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}