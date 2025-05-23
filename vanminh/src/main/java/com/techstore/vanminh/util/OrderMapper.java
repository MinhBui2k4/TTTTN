package com.techstore.vanminh.util;

import com.techstore.vanminh.dto.response.*;
import com.techstore.vanminh.entity.Order;
import com.techstore.vanminh.entity.OrderItem;
import com.techstore.vanminh.entity.OrderTimeline;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper {

    public Order toOrder(OrderRequestDTO requestDTO) {
        Order order = new Order();
        order.setShippingCost(requestDTO.getShippingCost());
        return order;
    }

    public OrderResponseDTO toOrderResponseDTO(Order order) {
        OrderResponseDTO responseDTO = new OrderResponseDTO();
        responseDTO.setId(order.getId());
        responseDTO.setUserId(order.getUser() != null ? order.getUser().getId() : null);
        responseDTO.setOrderDate(order.getOrderDate());
        responseDTO.setStatus(order.getStatus() != null ? order.getStatus().name() : null);
        responseDTO.setTotal(order.getTotal());
        responseDTO.setShippingCost(order.getShippingCost());
        responseDTO.setPaymentMethodId(order.getPaymentMethod() != null ? order.getPaymentMethod().getId() : null);
        responseDTO
                .setShippingAddressId(order.getShippingAddress() != null ? order.getShippingAddress().getId() : null);
        responseDTO.setItems(order.getItems().stream()
                .map(this::toOrderItemResponseDTO)
                .collect(Collectors.toList()));
        responseDTO.setTimeline(order.getTimeline().stream()
                .map(this::toOrderTimelineResponseDTO)
                .collect(Collectors.toList()));
        return responseDTO;
    }

    public OrderItem toOrderItem(OrderItemRequestDTO requestDTO) {
        OrderItem orderItem = new OrderItem();
        orderItem.setQuantity(requestDTO.getQuantity());
        return orderItem;
    }

    public OrderItemResponseDTO toOrderItemResponseDTO(OrderItem orderItem) {
        OrderItemResponseDTO responseDTO = new OrderItemResponseDTO();
        responseDTO.setId(orderItem.getId());
        responseDTO.setProductId(orderItem.getProduct() != null ? orderItem.getProduct().getId() : null);
        responseDTO.setProductName(orderItem.getProduct() != null ? orderItem.getProduct().getName() : null);
        responseDTO.setQuantity(orderItem.getQuantity());
        responseDTO.setPrice(orderItem.getPrice());
        return responseDTO;
    }

    public OrderTimelineResponseDTO toOrderTimelineResponseDTO(OrderTimeline timeline) {
        OrderTimelineResponseDTO responseDTO = new OrderTimelineResponseDTO();
        responseDTO.setId(timeline.getId());
        responseDTO.setStatus(timeline.getStatus() != null ? timeline.getStatus().name() : null);
        responseDTO.setDate(timeline.getDate());
        responseDTO.setDescription(timeline.getDescription());
        return responseDTO;
    }

    public List<OrderResponseDTO> toOrderResponseDTOList(List<Order> orders) {
        return orders.stream()
                .map(this::toOrderResponseDTO)
                .collect(Collectors.toList());
    }
}