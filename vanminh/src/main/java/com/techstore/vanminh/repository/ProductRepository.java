package com.techstore.vanminh.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.techstore.vanminh.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByNameContainingIgnoreCase(String name);
}