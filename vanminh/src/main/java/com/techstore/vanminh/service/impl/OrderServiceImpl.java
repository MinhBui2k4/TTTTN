package com.techstore.vanminh.service.impl;

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
import java.util.List;
import java.util.logging.Logger;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private static final Logger logger = Logger.getLogger(OrderServiceImpl.class.getName());

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

    @Override
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

        // Lưu Order trước để có ID
        orderRepository.saveAndFlush(order);
        logger.info("Saved order: " + order.getId());

        double total = 0;
        for (CartItem cartItem : cart.getItems()) {
            logger.info("Processing cart item: " + cartItem.getId());
            if (cartItem.getProduct() == null || cartItem.getProduct().getId() == null) {
                throw new BadRequestException("Cart item " + cartItem.getId() + " has no valid product");
            }

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
            logger.info("Created order item for product: " + product.getId());

            // Lưu OrderItem riêng
            orderItemRepository.saveAndFlush(orderItem);
            logger.info("Saved order item: " + orderItem.getId());

            product.setQuantity(product.getQuantity() - cartItem.getQuantity());
            productRepository.save(product);
            logger.info("Updated product quantity: " + product.getId());

            total += cartItem.getQuantity() * product.getPrice();
        }

        order.setTotal(total + shippingCost);
        logger.info("Set order total: " + order.getTotal());

        OrderTimeline timeline = new OrderTimeline();
        timeline.setOrder(order);
        timeline.setStatus(Order.OrderStatus.ORDERED);
        timeline.setDate(LocalDateTime.now());
        timeline.setDescription("Order placed");
        // Lưu OrderTimeline riêng
        orderTimelineRepository.saveAndFlush(timeline);
        logger.info("Saved timeline: " + timeline.getId());

        // Lưu lại Order
        orderRepository.saveAndFlush(order);
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

    @Override
    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO requestDTO) {
        logger.info(
                "Starting createOrder for user: " + SecurityContextHolder.getContext().getAuthentication().getName());
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found"));
        logger.info("Found user: " + user.getId());

        PaymentMethod paymentMethod = paymentMethodRepository.findById(requestDTO.getPaymentMethodId())
                .orElseThrow(() -> new BadRequestException("Payment method not found"));
        logger.info("Found payment method: " + requestDTO.getPaymentMethodId());

        Address shippingAddress = addressRepository.findById(requestDTO.getShippingAddressId())
                .orElseThrow(() -> new BadRequestException("Shipping address not found"));
        logger.info("Found shipping address: " + requestDTO.getShippingAddressId());

        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(Order.OrderStatus.ORDERED);
        order.setPaymentMethod(paymentMethod);
        order.setShippingAddress(shippingAddress);
        order.setShippingCost(requestDTO.getShippingCost());
        logger.info("Created order entity");

        // Lưu Order trước để có ID
        orderRepository.saveAndFlush(order);
        logger.info("Saved order: " + order.getId());

        double total = 0;
        for (OrderItemRequestDTO itemDTO : requestDTO.getItems()) {
            logger.info("Processing order item with productId: " + itemDTO.getProductId());
            Product product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new BadRequestException("Product not found: " + itemDTO.getProductId()));
            logger.info("Found product: " + product.getId());

            if (product.getQuantity() < itemDTO.getQuantity()) {
                throw new BadRequestException("Insufficient stock for product: " + product.getName());
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemDTO.getQuantity());
            orderItem.setPrice(product.getPrice());
            logger.info("Created order item for product: " + product.getId());

            // Lưu OrderItem riêng
            orderItemRepository.saveAndFlush(orderItem);
            logger.info("Saved order item: " + orderItem.getId());

            product.setQuantity(product.getQuantity() - itemDTO.getQuantity());
            productRepository.save(product);
            logger.info("Updated product quantity: " + product.getId());

            total += itemDTO.getQuantity() * product.getPrice();
        }

        order.setTotal(total + requestDTO.getShippingCost());
        logger.info("Set order total: " + order.getTotal());

        OrderTimeline timeline = new OrderTimeline();
        timeline.setOrder(order);
        timeline.setStatus(Order.OrderStatus.ORDERED);
        timeline.setDate(LocalDateTime.now());
        timeline.setDescription("Order placed");
        // Lưu OrderTimeline riêng
        orderTimelineRepository.saveAndFlush(timeline);
        logger.info("Saved timeline: " + timeline.getId());

        // Lưu lại Order
        orderRepository.saveAndFlush(order);
        logger.info("Final save order: " + order.getId());

        OrderResponseDTO responseDTO = orderMapper.toOrderResponseDTO(order);
        logger.info("Mapped to OrderResponseDTO");
        return responseDTO;
    }

    @Override
    public BaseResponse<OrderResponseDTO> getUserOrders(Pageable pageable) {
        logger.info(
                "Starting getUserOrders for user: " + SecurityContextHolder.getContext().getAuthentication().getName());
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tìm thấy"));
        logger.info("Found user: " + user.getId());

        Page<Order> orders = orderRepository.findByUserId(user.getId(), pageable);
        logger.info("Found " + orders.getTotalElements() + " orders for user: " + user.getId());

        BaseResponse<OrderResponseDTO> response = new BaseResponse<>();
        response.setContent(orderMapper.toOrderResponseDTOList(orders.getContent()));
        response.setPageNumber(orders.getNumber());
        response.setPageSize(orders.getSize());
        response.setTotalElements(orders.getTotalElements());
        response.setTotalPages(orders.getTotalPages());
        response.setLastPage(orders.isLast());
        logger.info("Completed getUserOrders for user: " + user.getId());

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

        List<OrderItem> items = orderItemRepository.findByOrderId(id);
        for (OrderItem item : items) {
            Product product = item.getProduct();
            product.setQuantity(product.getQuantity() + item.getQuantity());
            productRepository.save(product);
        }

        OrderTimeline timeline = new OrderTimeline();
        timeline.setOrder(order);
        timeline.setStatus(Order.OrderStatus.CANCELLED);
        timeline.setDate(LocalDateTime.now());
        timeline.setDescription("Đơn hàng đã bị hủy bởi người dùng");
        orderTimelineRepository.saveAndFlush(timeline);

        orderRepository.saveAndFlush(order);

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
            orderTimelineRepository.saveAndFlush(timeline);

            orderRepository.saveAndFlush(order);

            return orderMapper.toOrderResponseDTO(order);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Trạng thái không hợp lệ: " + status);
        }
    }

    @Override
    public BaseResponse<OrderResponseDTO> getOrdersByStatus(Order.OrderStatus status, Pageable pageable) {
        logger.info("Starting getOrdersByStatus for status: " + status);
        Page<Order> orders = orderRepository.findByStatus(status, pageable);
        logger.info("Found " + orders.getTotalElements() + " orders with status: " + status);

        BaseResponse<OrderResponseDTO> response = new BaseResponse<>();
        response.setContent(orderMapper.toOrderResponseDTOList(orders.getContent()));
        response.setPageNumber(orders.getNumber());
        response.setPageSize(orders.getSize());
        response.setTotalElements(orders.getTotalElements());
        response.setTotalPages(orders.getTotalPages());
        response.setLastPage(orders.isLast());
        logger.info("Completed getOrdersByStatus for status: " + status);

        return response;
    }
}