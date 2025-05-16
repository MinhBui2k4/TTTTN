package com.techstore.vanminh.service;

import com.techstore.vanminh.dto.AuthRequest;
import com.techstore.vanminh.dto.AuthResponse;
import com.techstore.vanminh.entity.User;
import com.techstore.vanminh.repository.UserRepository;
import com.techstore.vanminh.security.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.techstore.vanminh.exception.ResourceNotFoundException;

import java.util.stream.Collectors;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    public AuthResponse login(AuthRequest authRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getEmail(),
                        authRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userRepository.findByEmail(authRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtUtil.generateToken(
                user.getEmail(),
                user.getRoles().stream()
                        .map(role -> role.getName().toString())
                        .collect(Collectors.toList()));

        return new AuthResponse(token, user.getEmail());
    }
}