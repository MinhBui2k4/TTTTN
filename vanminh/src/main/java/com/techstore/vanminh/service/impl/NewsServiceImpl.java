package com.techstore.vanminh.service.impl;

import com.techstore.vanminh.dto.NewsDTO;
import com.techstore.vanminh.dto.response.BaseResponse;
import com.techstore.vanminh.entity.News;
import com.techstore.vanminh.entity.User;
import com.techstore.vanminh.exception.BadRequestException;
import com.techstore.vanminh.exception.ResourceNotFoundException;
import com.techstore.vanminh.repository.NewsRepository;
import com.techstore.vanminh.repository.UserRepository;
import com.techstore.vanminh.service.FileService;
import com.techstore.vanminh.service.NewsService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class NewsServiceImpl implements NewsService {

    @Autowired
    private NewsRepository newsRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileService fileService;

    @Autowired
    private ModelMapper modelMapper;

    private static final String NEWS_IMAGE_DIR = "Uploads/news";

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tìm thấy"));
    }

    @Override
    public NewsDTO createNews(NewsDTO newsDTO) {
        News news = modelMapper.map(newsDTO, News.class);

        // Xử lý hình ảnh
        if (newsDTO.getImageFile() != null && !newsDTO.getImageFile().isEmpty()) {
            try {
                String imageFileName = fileService.uploadImgNews(NEWS_IMAGE_DIR, newsDTO.getImageFile());
                news.setImage(imageFileName);
            } catch (IOException e) {
                throw new BadRequestException("Không thể tải lên hình ảnh tin tức: " + e.getMessage());
            }
        }

        news = newsRepository.save(news);
        return modelMapper.map(news, NewsDTO.class);
    }

    @Override
    public BaseResponse<NewsDTO> getAllNews(int pageNumber, int pageSize, String search) {
        List<News> allNews;

        if (search != null && !search.trim().isEmpty()) {
            allNews = newsRepository.findByTitleOrContentContainingIgnoreCase(search);
        } else {
            allNews = newsRepository.findAll();
        }

        int totalElements = allNews.size();
        int totalPages = (int) Math.ceil((double) totalElements / pageSize);
        int fromIndex = Math.min(pageNumber * pageSize, totalElements);
        int toIndex = Math.min(fromIndex + pageSize, totalElements);

        List<NewsDTO> pageContent = allNews.subList(fromIndex, toIndex).stream()
                .map(news -> modelMapper.map(news, NewsDTO.class))
                .collect(Collectors.toList());

        BaseResponse<NewsDTO> response = new BaseResponse<>();
        response.setContent(pageContent);
        response.setPageNumber(pageNumber);
        response.setPageSize(pageSize);
        response.setTotalElements((long) totalElements); // Set totalElements
        response.setTotalPages(totalPages);
        response.setLastPage(pageNumber >= totalPages - 1);
        return response;
    }

    @Override
    public NewsDTO getNewsById(Long id) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tin tức không tìm thấy với id: " + id));
        return modelMapper.map(news, NewsDTO.class);
    }

    @Override
    public NewsDTO updateNews(Long id, NewsDTO newsDTO) {
        News existingNews = newsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tin tức không tìm thấy với id: " + id));

        modelMapper.map(newsDTO, existingNews);

        // Xử lý hình ảnh
        if (newsDTO.getImageFile() != null && !newsDTO.getImageFile().isEmpty()) {
            try {
                String imageFileName = fileService.uploadImgNews(NEWS_IMAGE_DIR, newsDTO.getImageFile());
                existingNews.setImage(imageFileName);
            } catch (IOException e) {
                throw new BadRequestException("Không thể tải lên hình ảnh tin tức: " + e.getMessage());
            }
        }

        existingNews = newsRepository.save(existingNews);
        return modelMapper.map(existingNews, NewsDTO.class);
    }

    @Override
    public void deleteNews(Long id) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tin tức không tìm thấy với id: " + id));
        newsRepository.delete(news);
    }

    @Override
    public InputStream getNewsImage(String fileName) throws FileNotFoundException {
        return fileService.getResource(NEWS_IMAGE_DIR, fileName);
    }
}