package com.techstore.vanminh.repository;

import com.techstore.vanminh.entity.PaymentTransaction;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    Optional<PaymentTransaction> findByTransactionId(String transactionId);

    Optional<PaymentTransaction> findByMomoOrderId(String momoOrderId); // Thêm phương thức mới

    Optional<PaymentTransaction> findByOrderIdAndStatus(Long orderId, String status);
}