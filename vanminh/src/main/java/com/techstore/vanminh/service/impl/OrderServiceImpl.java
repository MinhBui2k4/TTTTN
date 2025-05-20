package com.techstore.vanminh.service.impl;

import com.techstore.vanminh.dto.OrderDTO;
import com.techstore.vanminh.dto.OrderItemDTO;
import com.techstore.vanminh.dto.OrderTimelineDTO;
import com.techstore.vanminh.dto.response.BaseResponse;
import com.techstore.vanminh.entity.*;
import com.techstore.vanminh.exception.BadRequestException;
import com.techstore.vanminh.exception.ResourceNotFoundException;
import com.techstore.vanminh.repository.*;
import com.techstore.vanminh.service.OrderService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

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
    private ModelMapper modelMapper;

    @Override
    public OrderDTO createOrder(OrderDTO orderDTO) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tìm thấy"));

        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Giỏ hàng không tìm thấy"));

        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Giỏ hàng trống");
        }

        Address shippingAddress = addressRepository.findByIdAndUserId(orderDTO.getShippingAddressId(), user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Địa chỉ giao hàng không tìm thấy hoặc không thuộc về bạn"));

        PaymentMethod paymentMethod = paymentMethodRepository.findById(orderDTO.getPaymentMethodId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Phương thức thanh toán không tìm thấy với id: " + orderDTO.getPaymentMethodId()));

        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(Order.OrderStatus.ORDERED);
        order.setShippingAddress(shippingAddress);
        order.setPaymentMethod(paymentMethod);
        order.setShippingCost(orderDTO.getShippingCost() != null ? orderDTO.getShippingCost() : 0.0);

        Set<OrderItem> orderItems = new HashSet<>();
        double total = 0.0;

        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            if (!product.isAvailability()) {
                throw new BadRequestException("Sản phẩm " + product.getName() + " hiện không có sẵn");
            }
            if (cartItem.getQuantity() > product.getQuantity()) {
                throw new BadRequestException(
                        "Số lượng sản phẩm " + product.getName() + " vượt quá tồn kho: " + product.getQuantity());
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(product.getPrice());
            orderItems.add(orderItem);

            total += product.getPrice() * cartItem.getQuantity();

            // Giảm số lượng tồn kho
            product.setQuantity(product.getQuantity() - cartItem.getQuantity());
            productRepository.save(product);
        }

        order.setItems(orderItems);
        order.setTotal(total + order.getShippingCost());

        // Tạo timeline
        OrderTimeline timeline = new OrderTimeline();
        timeline.setOrder(order);
        timeline.setStatus(Order.OrderStatus.ORDERED);
        timeline.setDate(LocalDateTime.now());
        timeline.setDescription("Đơn hàng đã được đặt");
        Set<OrderTimeline> timelines = new HashSet<>();
        timelines.add(timeline);
        order.setTimeline(timelines);

        order = orderRepository.save(order);

        // Xóa giỏ hàng
        cartItemRepository.deleteAll(cart.getItems());
        cart.getItems().clear();
        cartRepository.save(cart);

        return modelMapper.map(order, OrderDTO.class);
    }

    @Override
    public BaseResponse<OrderDTO> getUserOrders(Pageable pageable) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tìm thấy"));

        Page<Order> orders = orderRepository.findByUserId(user.getId(), pageable);

        BaseResponse<OrderDTO> response = new BaseResponse<>();
        response.setContent(orders.getContent().stream()
                .map(order -> modelMapper.map(order, OrderDTO.class))
                .collect(Collectors.toList()));
        response.setPageNumber(orders.getNumber());
        response.setPageSize(orders.getSize());
        response.setTotalElements(orders.getTotalElements());
        response.setTotalPages(orders.getTotalPages());
        response.setLastPage(orders.isLast());

        return response;
    }

    @Override
    public OrderDTO getOrderById(Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tìm thấy"));

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng không tìm thấy với id: " + id));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Bạn không có quyền xem đơn hàng này");
        }

        return modelMapper.map(order, OrderDTO.class);
    }

    @Override
    public OrderDTO cancelOrder(Long id) {
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

        // Khôi phục số lượng tồn kho
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.setQuantity(product.getQuantity() + item.getQuantity());
            productRepository.save(product);
        }

        // Cập nhật timeline
        OrderTimeline timeline = new OrderTimeline();
        timeline.setOrder(order);
        timeline.setStatus(Order.OrderStatus.CANCELLED);
        timeline.setDate(LocalDateTime.now());
        timeline.setDescription("Đơn hàng đã bị hủy bởi người dùng");
        order.getTimeline().add(timeline);

        order = orderRepository.save(order);

        return modelMapper.map(order, OrderDTO.class);
    }

    @Override
    public OrderDTO updateOrderStatus(Long id, String status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng không tìm thấy với id: " + id));

        try {
            Order.OrderStatus newStatus = Order.OrderStatus.valueOf(status.toUpperCase());
            if (newStatus == Order.OrderStatus.CANCELLED) {
                throw new BadRequestException(
                        "Admin không thể hủy đơn hàng. Vui lòng sử dụng endpoint hủy của người dùng");
            }

            order.setStatus(newStatus);

            // Cập nhật timeline
            OrderTimeline timeline = new OrderTimeline();
            timeline.setOrder(order);
            timeline.setStatus(newStatus);
            timeline.setDate(LocalDateTime.now());
            timeline.setDescription("Trạng thái đơn hàng cập nhật thành " + newStatus);
            order.getTimeline().add(timeline);

            order = orderRepository.save(order);

            return modelMapper.map(order, OrderDTO.class);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Trạng thái không hợp lệ: " + status);
        }
    }
}