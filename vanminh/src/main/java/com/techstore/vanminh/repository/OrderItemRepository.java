package com.techstore.vanminh.repository;

import com.techstore.vanminh.entity.OrderItem;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrderId(Long orderId);
}