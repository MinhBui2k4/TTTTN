package com.techstore.vanminh.service;

import java.io.FileNotFoundException;
import java.io.InputStream;

import com.techstore.vanminh.dto.NewsDTO;
import com.techstore.vanminh.dto.response.BaseResponse;

public interface NewsService {
    NewsDTO createNews(NewsDTO newsDTO);
    

    BaseResponse<NewsDTO> getAllNews(int pageNumber, int pageSize, String search);

    NewsDTO getNewsById(Long id);

    NewsDTO updateNews(Long id, NewsDTO newsDTO);

    void deleteNews(Long id);

    public InputStream getNewsImage(String fileName) throws FileNotFoundException;
}