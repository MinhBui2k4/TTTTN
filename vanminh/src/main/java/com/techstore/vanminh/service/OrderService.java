package com.techstore.vanminh.service;

import com.techstore.vanminh.dto.OrderDTO;
import com.techstore.vanminh.dto.response.BaseResponse;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    OrderDTO createOrder(OrderDTO orderDTO);

    BaseResponse<OrderDTO> getUserOrders(Pageable pageable);

    OrderDTO getOrderById(Long id);

    OrderDTO cancelOrder(Long id);

    OrderDTO updateOrderStatus(Long id, String status);
}