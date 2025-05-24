package com.techstore.vanminh.util;

import com.techstore.vanminh.dto.response.*;
import com.techstore.vanminh.entity.Order;
import com.techstore.vanminh.entity.OrderItem;
import com.techstore.vanminh.entity.OrderTimeline;
import com.techstore.vanminh.repository.OrderItemRepository;
import com.techstore.vanminh.repository.OrderTimelineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Component
public class OrderMapper {

    private static final Logger logger = Logger.getLogger(OrderMapper.class.getName());

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderTimelineRepository orderTimelineRepository;

    public Order toOrder(OrderRequestDTO requestDTO) {
        logger.info("Mapping OrderRequestDTO to Order");
        Order order = new Order();
        order.setShippingCost(requestDTO.getShippingCost());
        return order;
    }

    public OrderResponseDTO toOrderResponseDTO(Order order) {
        logger.info("Mapping Order to OrderResponseDTO: " + order.getId());
        OrderResponseDTO responseDTO = new OrderResponseDTO();
        responseDTO.setId(order.getId());
        responseDTO.setUserId(order.getUser() != null ? order.getUser().getId() : null);
        responseDTO.setOrderDate(order.getOrderDate());
        responseDTO.setStatus(order.getStatus() != null ? order.getStatus().name() : null);
        responseDTO.setTotal(order.getTotal());
        responseDTO.setShippingCost(order.getShippingCost());

        // Log để kiểm tra paymentMethod và shippingAddress
        logger.info("PaymentMethod: " + (order.getPaymentMethod() != null ? order.getPaymentMethod().getId() : "null"));
        logger.info("ShippingAddress: "
                + (order.getShippingAddress() != null ? order.getShippingAddress().getId() : "null"));
        responseDTO.setPaymentMethodId(order.getPaymentMethod() != null ? order.getPaymentMethod().getId() : null);
        responseDTO
                .setShippingAddressId(order.getShippingAddress() != null ? order.getShippingAddress().getId() : null);

        // Lấy OrderItems từ database
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        logger.info("Found " + items.size() + " OrderItems for order: " + order.getId());
        responseDTO.setItems(items.isEmpty() ? null
                : items.stream()
                        .map(this::toOrderItemResponseDTO)
                        .collect(Collectors.toList()));

        // Lấy OrderTimelines từ database
        List<OrderTimeline> timelines = orderTimelineRepository.findByOrderId(order.getId());
        logger.info("Found " + timelines.size() + " OrderTimelines for order: " + order.getId());
        responseDTO.setTimeline(timelines.isEmpty() ? null
                : timelines.stream()
                        .map(this::toOrderTimelineResponseDTO)
                        .collect(Collectors.toList()));

        logger.info("Completed mapping Order to OrderResponseDTO: " + order.getId());
        return responseDTO;
    }

    public OrderItem toOrderItem(OrderItemRequestDTO requestDTO) {
        logger.info("Mapping OrderItemRequestDTO to OrderItem");
        OrderItem orderItem = new OrderItem();
        orderItem.setQuantity(requestDTO.getQuantity());
        return orderItem;
    }

    public OrderItemResponseDTO toOrderItemResponseDTO(OrderItem orderItem) {
        logger.info("Mapping OrderItem to OrderItemResponseDTO: " + orderItem.getId());
        OrderItemResponseDTO responseDTO = new OrderItemResponseDTO();
        responseDTO.setId(orderItem.getId());
        responseDTO.setProductId(orderItem.getProduct() != null ? orderItem.getProduct().getId() : null);
        responseDTO.setProductName(orderItem.getProduct() != null ? orderItem.getProduct().getName() : null);
        responseDTO.setQuantity(orderItem.getQuantity());
        responseDTO.setPrice(orderItem.getPrice());
        return responseDTO;
    }

    public OrderTimelineResponseDTO toOrderTimelineResponseDTO(OrderTimeline timeline) {
        logger.info("Mapping OrderTimeline to OrderTimelineResponseDTO: " + timeline.getId());
        OrderTimelineResponseDTO responseDTO = new OrderTimelineResponseDTO();
        responseDTO.setId(timeline.getId());
        responseDTO.setStatus(timeline.getStatus() != null ? timeline.getStatus().name() : null);
        responseDTO.setDate(timeline.getDate());
        responseDTO.setDescription(timeline.getDescription());
        return responseDTO;
    }

    public List<OrderResponseDTO> toOrderResponseDTOList(List<Order> orders) {
        logger.info("Mapping list of Orders to OrderResponseDTOs");
        return orders.stream()
                .map(this::toOrderResponseDTO)
                .collect(Collectors.toList());
    }
}