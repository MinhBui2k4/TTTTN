package com.techstore.vanminh.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.techstore.vanminh.entity.Cart;

public interface CartRepository extends JpaRepository<Cart, Long> {
}
