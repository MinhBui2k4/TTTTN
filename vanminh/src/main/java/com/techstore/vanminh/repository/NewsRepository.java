package com.techstore.vanminh.repository;

import com.techstore.vanminh.entity.News;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NewsRepository extends JpaRepository<News, Long> {
    Page<News> findAll(Pageable pageable);

    @Query("SELECT n FROM News n WHERE LOWER(n.title) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(n.content) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<News> findByTitleOrContentContainingIgnoreCase(@Param("search") String search);
}