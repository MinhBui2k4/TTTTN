package com.techstore.vanminh.service;

import com.techstore.vanminh.dto.ProductDTO;
import com.techstore.vanminh.dto.response.BaseResponse;

import java.io.FileNotFoundException;
import java.io.InputStream;

import org.springframework.data.domain.Pageable;

public interface ProductService {
    public BaseResponse<ProductDTO> getAllProducts(Pageable pageable, Long categoryId, Long brandId, String search,
            Double priceStart, Double priceEnd);

    ProductDTO getProductById(Long id);

    ProductDTO createProduct(ProductDTO productDTO);

    ProductDTO updateProduct(Long id, ProductDTO productDTO);

    void deleteProduct(Long id);

    BaseResponse<ProductDTO> getProductIsNews(Pageable pageable);

    BaseResponse<ProductDTO> getProductIsSales(Pageable pageable);

    BaseResponse<ProductDTO> findByCategoryCategoryId(Long categoryId, Pageable pageable);

    BaseResponse<ProductDTO> getProductBySearch(String search, Pageable pageable);

    public InputStream getProductImage(String fileName) throws FileNotFoundException;

    public InputStream getProductImages(String fileNames) throws FileNotFoundException;
}