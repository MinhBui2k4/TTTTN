package com.techstore.vanminh.repository;

import com.techstore.vanminh.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT o FROM Order o WHERE o.user.id = :userId")
    Page<Order> findByUserId(Long userId, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.status = :status")
    Page<Order> findByStatus(Order.OrderStatus status, Pageable pageable);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.shippingAddress.id = :addressId")
    long countByShippingAddressId(Long addressId);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.paymentMethod.id = :paymentMethodId")
    long countByPaymentMethodId(Long paymentMethodId);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.user.id = :userId")
    long countByUserId(Long userId);
}