package com.techstore.vanminh.service;

import com.techstore.vanminh.dto.NewsDTO;
import com.techstore.vanminh.dto.response.BaseResponse;
import org.springframework.data.domain.Pageable;

public interface NewsService {
    NewsDTO createNews(NewsDTO newsDTO);

    BaseResponse<NewsDTO> getAllNews(int pageNumber, int pageSize, String search);

    NewsDTO getNewsById(Long id);

    NewsDTO updateNews(Long id, NewsDTO newsDTO);

    void deleteNews(Long id);
}