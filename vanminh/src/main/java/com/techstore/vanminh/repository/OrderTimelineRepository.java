package com.techstore.vanminh.repository;

import com.techstore.vanminh.entity.OrderTimeline;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderTimelineRepository extends JpaRepository<OrderTimeline, Long> {
    List<OrderTimeline> findByOrderId(Long orderId);
}