package com.techstore.vanminh.service;

import com.techstore.vanminh.dto.response.*;
import com.techstore.vanminh.entity.Order;

import org.springframework.data.domain.Pageable;

public interface OrderService {
    OrderResponseDTO createOrder(OrderRequestDTO orderRequestDTO);

    OrderResponseDTO createOrderFromCart(Long paymentMethodId, Long shippingAddressId, Double shippingCost);

    BaseResponse<OrderResponseDTO> getOrdersByStatus(Order.OrderStatus status, Pageable pageable);

    BaseResponse<OrderResponseDTO> getUserOrders(Pageable pageable);

    OrderResponseDTO getOrderById(Long id);

    OrderResponseDTO cancelOrder(Long id);

    OrderResponseDTO updateOrderStatus(Long id, String status);

    BaseResponse<OrderResponseDTO> getAllOrders(Pageable pageable);

    BaseResponse<OrderResponseDTO> getOrdersByUserIdAndStatus(Long userId, Order.OrderStatus status, Pageable pageable);

    Order findOrderEntityById(Long id); // New method to fetch Order entity
}