package com.techstore.vanminh.controller;

import com.techstore.vanminh.dto.response.*;
import com.techstore.vanminh.entity.Order;
import com.techstore.vanminh.service.OrderService;

import java.util.Arrays;
import java.util.List;

import com.techstore.vanminh.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(@Valid @RequestBody OrderRequestDTO orderRequestDTO) {
        return ResponseEntity.ok(orderService.createOrder(orderRequestDTO));
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/status/{status}")
    public ResponseEntity<BaseResponse<OrderResponseDTO>> getOrdersByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "orderDate") String sort,
            @RequestParam(defaultValue = "asc") String sortOrder) {
        try {
            Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(status.toUpperCase());
            List<String> validSortFields = Arrays.asList("id", "orderDate", "total", "status", "shippingCost");
            String sortField = validSortFields.contains(sort) ? sort : "orderDate";

            Sort sortObj = sortOrder.equalsIgnoreCase("desc")
                    ? Sort.by(sortField).descending()
                    : Sort.by(sortField).ascending();

            Pageable pageable = PageRequest.of(page, size, sortObj);

            return ResponseEntity.ok(orderService.getOrdersByStatus(orderStatus, pageable));
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid order status: " + status);
        }
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/from-cart")
    public ResponseEntity<OrderResponseDTO> createOrderFromCart(
            @RequestParam @Positive(message = "Payment method ID must be positive") Long paymentMethodId,
            @RequestParam @Positive(message = "Shipping address ID must be positive") Long shippingAddressId,
            @RequestParam @PositiveOrZero(message = "Shipping cost must be positive or zero") Double shippingCost) {
        return ResponseEntity.ok(orderService.createOrderFromCart(paymentMethodId, shippingAddressId, shippingCost));
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping
    public ResponseEntity<BaseResponse<OrderResponseDTO>> getUserOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "orderDate") String sort,
            @RequestParam(defaultValue = "asc") String sortOrder) {
        // Danh sách các thuộc tính hợp lệ để sắp xếp
        List<String> validSortFields = Arrays.asList("id", "orderDate", "total", "status", "shippingCost");

        // Kiểm tra sort field có hợp lệ không, nếu không thì dùng mặc định
        String sortField = validSortFields.contains(sort) ? sort : "orderDate";

        // Tạo Sort object
        Sort sortObj = sortOrder.equalsIgnoreCase("desc")
                ? Sort.by(sortField).descending()
                : Sort.by(sortField).ascending();

        // Tạo Pageable
        Pageable pageable = PageRequest.of(page, size, sortObj);

        return ResponseEntity.ok(orderService.getUserOrders(pageable));
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PutMapping("/{id}/cancel")
    public ResponseEntity<OrderResponseDTO> cancelOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.cancelOrder(id));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/admin/{id}/status")
    public ResponseEntity<OrderResponseDTO> updateOrderStatus(@PathVariable Long id,
            @RequestBody StatusUpdateRequest request) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, request.getStatus()));
    }

    public static class StatusUpdateRequest {
        private String status;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}