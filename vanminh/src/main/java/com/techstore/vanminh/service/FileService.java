package com.techstore.vanminh.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public interface FileService {

    String uploadAvatar(String path, MultipartFile file, Long userId) throws IOException;

    String uploadImgNews(String path, MultipartFile file) throws IOException;

    String uploadImgProduct(String path, MultipartFile file) throws IOException;

    String uploadImgProducts(String path, MultipartFile file, int index) throws IOException;
    
    String uploadImgHeroSection(String path, MultipartFile file) throws IOException;

    InputStream getResource(String path, String fileName) throws FileNotFoundException;
}
