package com.techstore.vanminh.repository;

import com.techstore.vanminh.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId")
    Page<Product> findByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.brand.id = :brandId")
    Page<Product> findByBrandId(@Param("brandId") Long brandId, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Product> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.isNew = true")
    Page<Product> findByIsNewTrue(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.isSale = true")
    Page<Product> findByIsSaleTrue(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.availability = true")
    Page<Product> findByIsAvailabilityTrue(Pageable pageable);

    //
    @Query("SELECT p FROM Product p " +
            "WHERE (:categoryId IS NULL OR p.category.id = :categoryId) " +
            "AND (:brandId IS NULL OR p.brand.id = :brandId) " +
            "AND (:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:priceStart IS NULL OR p.price >= :priceStart) " +
            "AND (:priceEnd IS NULL OR p.price <= :priceEnd)")
    Page<Product> findWithFilters(@Param("categoryId") Long categoryId,
            @Param("brandId") Long brandId,
            @Param("search") String search,
            @Param("priceStart") Double priceStart,
            @Param("priceEnd") Double priceEnd,
            Pageable pageable);

}