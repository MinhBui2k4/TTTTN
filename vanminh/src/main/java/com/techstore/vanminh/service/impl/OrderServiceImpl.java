package com.techstore.vanminh.service.impl;

import com.techstore.vanminh.controller.UserController;
import com.techstore.vanminh.dto.response.*;
import com.techstore.vanminh.entity.*;
import com.techstore.vanminh.exception.BadRequestException;
import com.techstore.vanminh.exception.ResourceNotFoundException;
import com.techstore.vanminh.repository.*;
import com.techstore.vanminh.service.OrderService;
import com.techstore.vanminh.util.OrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private static final Logger logger = Logger.getLogger(UserController.class.getName());

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderTimelineRepository orderTimelineRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    @Autowired
    private OrderMapper orderMapper;

    @Transactional
    public OrderResponseDTO createOrderFromCart(Long paymentMethodId, Long shippingAddressId, Double shippingCost) {
        logger.info("Starting createOrderFromCart for user: "
                + SecurityContextHolder.getContext().getAuthentication().getName());
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found"));
        logger.info("Found user: " + user.getId());

        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BadRequestException("Cart not found"));
        logger.info("Found cart: " + cart.getId());

        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Cart is empty");
        }

        PaymentMethod paymentMethod = paymentMethodRepository.findById(paymentMethodId)
                .orElseThrow(() -> new BadRequestException("Payment method not found"));
        logger.info("Found payment method: " + paymentMethodId);

        Address shippingAddress = addressRepository.findById(shippingAddressId)
                .orElseThrow(() -> new BadRequestException("Shipping address not found"));
        logger.info("Found shipping address: " + shippingAddressId);

        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(Order.OrderStatus.ORDERED);
        order.setPaymentMethod(paymentMethod);
        order.setShippingAddress(shippingAddress);
        order.setShippingCost(shippingCost);
        logger.info("Created order entity");

        // Lưu Order trước
        orderRepository.saveAndFlush(order);
        logger.info("Saved order: " + order.getId());

        double total = 0;
        Set<OrderItem> orderItems = new HashSet<>();
        for (CartItem cartItem : cart.getItems()) {
            logger.info("Processing cart item: " + cartItem.getId());
            Product product = productRepository.findById(cartItem.getProduct().getId())
                    .orElseThrow(() -> new BadRequestException("Product not found: " + cartItem.getProduct().getId()));
            logger.info("Found product: " + product.getId());

            if (product.getQuantity() < cartItem.getQuantity()) {
                throw new BadRequestException("Insufficient stock for product: " + product.getName());
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(product.getPrice());
            logger.info("Created order item: " + orderItem.getId());

            orderItems.add(orderItem);
            logger.info("Added order item to set");

            // Lưu OrderItem riêng
            orderItemRepository.save(orderItem);
            logger.info("Saved order item: " + orderItem.getId());

            product.setQuantity(product.getQuantity() - cartItem.getQuantity());
            productRepository.save(product);
            logger.info("Updated product quantity: " + product.getId());

            total += cartItem.getQuantity() * product.getPrice();
        }
        order.setItems(orderItems);
        order.setTotal(total + shippingCost);
        logger.info("Set order items and total");

        OrderTimeline timeline = new OrderTimeline();
        timeline.setOrder(order);
        timeline.setStatus(Order.OrderStatus.ORDERED);
        timeline.setDate(LocalDateTime.now());
        timeline.setDescription("Order placed");
        order.getTimeline().add(timeline);
        logger.info("Added timeline");

        // Lưu lại Order
        orderRepository.save(order);
        logger.info("Final save order: " + order.getId());

        // Xóa giỏ hàng
        cartItemRepository.deleteAll(cart.getItems());
        cart.getItems().clear();
        cartRepository.save(cart);
        logger.info("Cleared cart");

        OrderResponseDTO responseDTO = orderMapper.toOrderResponseDTO(order);
        logger.info("Mapped to OrderResponseDTO");
        return responseDTO;
    }

    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO orderRequestDTO) {
        User user = userRepository.findById(orderRequestDTO.getUserId())
                .orElseThrow(() -> new BadRequestException("User not found"));

        PaymentMethod paymentMethod = paymentMethodRepository.findById(orderRequestDTO.getPaymentMethodId())
                .orElseThrow(() -> new BadRequestException("Payment method not found"));

        Address shippingAddress = addressRepository.findById(orderRequestDTO.getShippingAddressId())
                .orElseThrow(() -> new BadRequestException("Shipping address not found"));

        Order order = orderMapper.toOrder(orderRequestDTO);
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(Order.OrderStatus.ORDERED);
        order.setPaymentMethod(paymentMethod);
        order.setShippingAddress(shippingAddress);

        double total = 0;
        Set<OrderItem> orderItems = new HashSet<>();
        for (OrderItemRequestDTO itemDTO : orderRequestDTO.getItems()) {
            Product product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new BadRequestException("Product not found: " + itemDTO.getProductId()));

            if (product.getQuantity() < itemDTO.getQuantity()) {
                throw new BadRequestException("Insufficient stock for product: " + product.getName());
            }

            OrderItem orderItem = orderMapper.toOrderItem(itemDTO);
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setPrice(product.getPrice());
            orderItems.add(orderItem);

            product.setQuantity(product.getQuantity() - itemDTO.getQuantity());
            productRepository.save(product);

            total += itemDTO.getQuantity() * product.getPrice();
        }
        order.setItems(orderItems);
        order.setTotal(total + orderRequestDTO.getShippingCost());

        OrderTimeline timeline = new OrderTimeline();
        timeline.setOrder(order);
        timeline.setStatus(Order.OrderStatus.ORDERED);
        timeline.setDate(LocalDateTime.now());
        timeline.setDescription("Order placed");
        order.getTimeline().add(timeline);

        orderRepository.save(order);

        return orderMapper.toOrderResponseDTO(order);
    }

    @Override
    public BaseResponse<OrderResponseDTO> getUserOrders(Pageable pageable) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tìm thấy"));

        Page<Order> orders = orderRepository.findByUserId(user.getId(), pageable);

        BaseResponse<OrderResponseDTO> response = new BaseResponse<>();
        response.setContent(orderMapper.toOrderResponseDTOList(orders.getContent()));
        response.setPageNumber(orders.getNumber());
        response.setPageSize(orders.getSize());
        response.setTotalElements(orders.getTotalElements());
        response.setTotalPages(orders.getTotalPages());
        response.setLastPage(orders.isLast());

        return response;
    }

    @Override
    public OrderResponseDTO getOrderById(Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tìm thấy"));

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng không tìm thấy với id: " + id));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Bạn không có quyền xem đơn hàng này");
        }

        return orderMapper.toOrderResponseDTO(order);
    }

    @Override
    public OrderResponseDTO cancelOrder(Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tìm thấy"));

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng không tìm thấy với id: " + id));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Bạn không có quyền hủy đơn hàng này");
        }

        if (order.getStatus() != Order.OrderStatus.ORDERED && order.getStatus() != Order.OrderStatus.CONFIRMED) {
            throw new BadRequestException("Không thể hủy đơn hàng ở trạng thái " + order.getStatus());
        }

        order.setStatus(Order.OrderStatus.CANCELLED);

        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.setQuantity(product.getQuantity() + item.getQuantity());
            productRepository.save(product);
        }

        OrderTimeline timeline = new OrderTimeline();
        timeline.setOrder(order);
        timeline.setStatus(Order.OrderStatus.CANCELLED);
        timeline.setDate(LocalDateTime.now());
        timeline.setDescription("Đơn hàng đã bị hủy bởi người dùng");
        order.getTimeline().add(timeline);

        order = orderRepository.save(order);

        return orderMapper.toOrderResponseDTO(order);
    }

    @Override
    public OrderResponseDTO updateOrderStatus(Long id, String status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng không tìm thấy với id: " + id));

        try {
            Order.OrderStatus newStatus = Order.OrderStatus.valueOf(status.toUpperCase());
            if (newStatus == Order.OrderStatus.CANCELLED) {
                throw new BadRequestException(
                        "Admin không thể hủy đơn hàng. Vui lòng sử dụng endpoint hủy của người dùng");
            }

            order.setStatus(newStatus);

            OrderTimeline timeline = new OrderTimeline();
            timeline.setOrder(order);
            timeline.setStatus(newStatus);
            timeline.setDate(LocalDateTime.now());
            timeline.setDescription("Trạng thái đơn hàng cập nhật thành " + newStatus);
            order.getTimeline().add(timeline);

            order = orderRepository.save(order);

            return orderMapper.toOrderResponseDTO(order);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Trạng thái không hợp lệ: " + status);
        }
    }
}