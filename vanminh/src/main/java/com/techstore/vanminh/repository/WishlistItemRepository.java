package com.techstore.vanminh.repository;

import com.techstore.vanminh.entity.WishlistItem;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {
    Optional<WishlistItem> findByWishlistIdAndProductId(Long wishlistId, Long productId);

    Page<WishlistItem> findByWishlistId(Long wishlistId, Pageable pageable);
}