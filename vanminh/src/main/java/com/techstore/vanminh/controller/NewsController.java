package com.techstore.vanminh.controller;

import com.techstore.vanminh.dto.NewsDTO;
import com.techstore.vanminh.dto.response.BaseResponse;
import com.techstore.vanminh.service.NewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/news")
public class NewsController {

    @Autowired
    private NewsService newsService;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<NewsDTO> createNews(@Valid @ModelAttribute NewsDTO newsDTO) {
        return ResponseEntity.ok(newsService.createNews(newsDTO));
    }

    @GetMapping
    public ResponseEntity<BaseResponse<NewsDTO>> getAllNews(
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(newsService.getAllNews(pageNumber, pageSize, search));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NewsDTO> getNews(@PathVariable Long id) {
        return ResponseEntity.ok(newsService.getNewsById(id));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping(value = "/{id}", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<NewsDTO> updateNews(@PathVariable Long id, @Valid @ModelAttribute NewsDTO newsDTO) {
        return ResponseEntity.ok(newsService.updateNews(id, newsDTO));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNews(@PathVariable Long id) {
        newsService.deleteNews(id);
        return ResponseEntity.noContent().build();
    }
}