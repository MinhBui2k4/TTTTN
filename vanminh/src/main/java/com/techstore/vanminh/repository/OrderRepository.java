package com.techstore.vanminh.repository;

import com.techstore.vanminh.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrderRepository extends JpaRepository<Order, Long> {
    // @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.user.id
    // =:userId")
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items LEFT JOIN FETCH o.timeline WHERE o.user.id = :userId")
    Page<Order> findByUserId(Long userId, Pageable pageable);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.shippingAddress.id = :addressId")
    long countByShippingAddressId(Long addressId);

}