package com.techstore.vanminh.repository;

import com.techstore.vanminh.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    @Query("SELECT w FROM Wishlist w LEFT JOIN FETCH w.items WHERE w.user.id = :userId")
    Optional<Wishlist> findByUserId(Long userId);
}