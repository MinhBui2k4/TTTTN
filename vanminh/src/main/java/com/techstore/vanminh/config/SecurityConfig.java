package com.techstore.vanminh.config;

import com.techstore.vanminh.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        @Autowired
        private JwtAuthenticationFilter jwtAuthenticationFilter;

        // Public endpoints (no authentication required)
        private static final String[] PUBLIC_ENDPOINTS = {
                        "/api/auth/**",
                        "/api/products/**",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/webjars/**",
                        "/api/contacts/**",
                        "/api/news/**"
                        // "/api/admin/roles/**",
        };

        // Endpoints only for ADMIN
        private static final String[] ADMIN_ENDPOINTS = {
                        "/api/admin/**",
                        "/api/admin/categories/**",
                        "/api/admin/brands/**",
                        "/api/admin/roles/**",
                        "/api/admin/payment-methods/**",

        };

        // Endpoints for both ADMIN and USER
        private static final String[] ADMIN_USER_ENDPOINTS = {
                        "/api/users/**",
                        "/api/users/cart/**",
                        "/api/users/addresses/**",
                        "/api/orders/**",
                        "/api/users/wishlist/**",
        };

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(csrf -> csrf.disable())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                // Trong SecurityConfig
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                                                .requestMatchers(ADMIN_ENDPOINTS).hasAuthority("ROLE_ADMIN")
                                                .requestMatchers(ADMIN_USER_ENDPOINTS)
                                                .hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                                                .anyRequest().authenticated())
                                .exceptionHandling(ex -> ex
                                                .authenticationEntryPoint((req, res, e) -> {
                                                        res.setStatus(HttpStatus.UNAUTHORIZED.value());
                                                        res.setContentType("application/json");
                                                        res.getWriter().write(
                                                                        "{\"error\": \"Unauthorized: Invalid or expired token\"}");
                                                })
                                                .accessDeniedHandler((req, res, e) -> {
                                                        res.setStatus(HttpStatus.FORBIDDEN.value());
                                                        res.setContentType("application/json");
                                                        res.getWriter().write(
                                                                        "{\"error\": \"Forbidden: Insufficient permissions\"}");
                                                }))
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
                return http.build();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
                        throws Exception {
                return authenticationConfiguration.getAuthenticationManager();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOriginPatterns(List.of("*"));
                configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
                configuration.setAllowCredentials(true);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }
}