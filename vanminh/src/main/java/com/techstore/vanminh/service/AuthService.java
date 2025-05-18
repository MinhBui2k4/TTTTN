// package com.techstore.vanminh.service;

// import com.techstore.vanminh.dto.UserDTO;
// import com.techstore.vanminh.entity.Role;
// import com.techstore.vanminh.entity.User;
// import com.techstore.vanminh.exception.BadRequestException;
// import com.techstore.vanminh.exception.EmailNotFoundException;
// import com.techstore.vanminh.repository.RoleRepository;
// import com.techstore.vanminh.repository.UserRepository;
// import com.techstore.vanminh.security.JwtUtil;
// import org.modelmapper.ModelMapper;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.security.authentication.AuthenticationManager;
// import
// org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
// import org.springframework.security.core.Authentication;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.stereotype.Service;

// import java.util.Collections;
// import java.util.List;

// @Service
// public class AuthService {

// @Autowired
// private UserRepository userRepository;

// @Autowired
// private RoleRepository roleRepository;

// @Autowired
// private ModelMapper modelMapper;

// @Autowired
// private PasswordEncoder passwordEncoder;

// @Autowired
// private AuthenticationManager authenticationManager;

// @Autowired
// private JwtUtil jwtUtil;

// // Đăng ký người dùng
// public UserDTO register(UserDTO userDTO) {
// if (userRepository.findByEmail(userDTO.getEmail()).isPresent()) {
// throw new BadRequestException("Email already exists");
// }

// User user = modelMapper.map(userDTO, User.class);
// user.setPassword(passwordEncoder.encode(userDTO.getPassword()));

// // Gán vai trò USER mặc định
// Role userRole = roleRepository.findByName(Role.RoleName.USER)
// .orElseThrow(() -> new BadRequestException("Role USER not found"));
// user.setRoles(Collections.singletonList(userRole));

// user = userRepository.save(user);
// return modelMapper.map(user, UserDTO.class);
// }

// // Đăng nhập
// public String login(String email, String password) {
// User user = userRepository.findByEmail(email)
// .orElseThrow(() -> new EmailNotFoundException("Email not found: " + email));

// Authentication authentication = authenticationManager.authenticate(
// new UsernamePasswordAuthenticationToken(email, password));
// if (authentication.isAuthenticated()) {
// List<String> roles = user.getRoles().stream()
// .map(role -> role.getName().name())
// .toList();
// return jwtUtil.generateToken(email, roles);
// } else {
// throw new BadRequestException("Invalid credentials");
// }
// }
// }