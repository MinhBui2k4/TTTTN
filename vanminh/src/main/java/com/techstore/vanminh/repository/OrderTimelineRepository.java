package com.techstore.vanminh.repository;

import com.techstore.vanminh.entity.OrderTimeline;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderTimelineRepository extends JpaRepository<OrderTimeline, Long> {
}