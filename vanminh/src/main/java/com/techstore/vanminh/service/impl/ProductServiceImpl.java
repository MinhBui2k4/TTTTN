package com.techstore.vanminh.service.impl;

import com.techstore.vanminh.dto.ProductDTO;
import com.techstore.vanminh.dto.response.BaseResponse;
import com.techstore.vanminh.entity.Brand;
import com.techstore.vanminh.entity.Category;
import com.techstore.vanminh.entity.Product;
import com.techstore.vanminh.exception.BadRequestException;
import com.techstore.vanminh.exception.ResourceNotFoundException;
import com.techstore.vanminh.repository.BrandRepository;
import com.techstore.vanminh.repository.CategoryRepository;
import com.techstore.vanminh.repository.ProductRepository;
import com.techstore.vanminh.service.FileService;
import com.techstore.vanminh.service.ProductService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private FileService fileService;

    private static final String PRODUCT_IMAGE_DIR = "uploads/product";
    private static final String PRODUCT_IMAGES_DIR = "uploads/products";

    @Override
    public BaseResponse<ProductDTO> getAllProducts(Pageable pageable, Long categoryId, Long brandId, String search,
            Double priceStart, Double priceEnd) {
        Page<Product> products;

        // Trường hợp có lọc theo price hoặc nhiều điều kiện
        if (categoryId != null || brandId != null || search != null || priceStart != null || priceEnd != null) {
            products = productRepository.findWithFilters(categoryId, brandId, search, priceStart, priceEnd, pageable);
        } else {
            products = productRepository.findAll(pageable);
        }

        BaseResponse<ProductDTO> response = new BaseResponse<>();
        response.setContent(products.getContent().stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .collect(Collectors.toList()));
        response.setPageNumber(products.getNumber());
        response.setPageSize(products.getSize());
        response.setTotalElements(products.getTotalElements());
        response.setTotalPages(products.getTotalPages());
        response.setLastPage(products.isLast());

        return response;
    }

    @Override
    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tìm thấy với id: " + id));
        return modelMapper.map(product, ProductDTO.class);
    }

    @Override
    public ProductDTO createProduct(ProductDTO productDTO) {
        Product product = modelMapper.map(productDTO, Product.class);

        // Xử lý category
        if (productDTO.getCategoryId() != null) {
            Category category = categoryRepository.findById(productDTO.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Danh mục không tìm thấy với id: " + productDTO.getCategoryId()));
            product.setCategory(category);
        }

        // Xử lý brand
        if (productDTO.getBrandId() != null) {
            Brand brand = brandRepository.findById(productDTO.getBrandId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Thương hiệu không tìm thấy với id: " + productDTO.getBrandId()));
            product.setBrand(brand);
        }

        // Xử lý image
        if (productDTO.getImageFile() != null && !productDTO.getImageFile().isEmpty()) {
            try {
                String imageFileName = fileService.uploadImgProduct(PRODUCT_IMAGE_DIR, productDTO.getImageFile());
                product.setImage(imageFileName);
            } catch (IOException e) {
                throw new BadRequestException("Không thể tải lên hình ảnh sản phẩm: " + e.getMessage());
            }
        }

        // Xử lý images
        if (productDTO.getImageFiles() != null && !productDTO.getImageFiles().isEmpty()) {
            List<String> imageFileNames = new ArrayList<>();
            int index = 1;
            for (MultipartFile file : productDTO.getImageFiles()) {
                try {
                    String imageFileName = fileService.uploadImgProducts(PRODUCT_IMAGES_DIR, file, index);
                    imageFileNames.add(imageFileName);
                    index++;
                } catch (IOException e) {
                    throw new BadRequestException("Không thể tải lên hình ảnh sản phẩm: " + e.getMessage());
                }
            }

            product.setImages(imageFileNames);
        }

        product = productRepository.save(product);
        return modelMapper.map(product, ProductDTO.class);
    }

    @Override
    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tìm thấy với id: " + id));

        modelMapper.map(productDTO, existingProduct);

        // Xử lý category
        if (productDTO.getCategoryId() != null) {
            Category category = categoryRepository.findById(productDTO.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Danh mục không tìm thấy với id: " + productDTO.getCategoryId()));
            existingProduct.setCategory(category);
        }

        // Xử lý brand
        if (productDTO.getBrandId() != null) {
            Brand brand = brandRepository.findById(productDTO.getBrandId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Thương hiệu không tìm thấy với id: " + productDTO.getBrandId()));
            existingProduct.setBrand(brand);
        }

        // Xử lý image
        if (productDTO.getImageFile() != null && !productDTO.getImageFile().isEmpty()) {
            try {
                String imageFileName = fileService.uploadImgProduct(PRODUCT_IMAGE_DIR, productDTO.getImageFile());
                existingProduct.setImage(imageFileName);
            } catch (IOException e) {
                throw new BadRequestException("Không thể tải lên hình ảnh sản phẩm: " + e.getMessage());
            }
        }

        // Xử lý images
        if (productDTO.getImageFiles() != null && !productDTO.getImageFiles().isEmpty()) {
            List<String> imageFileNames = new ArrayList<>();
            int index = 1;
            for (MultipartFile file : productDTO.getImageFiles()) {
                try {
                    String imageFileName = fileService.uploadImgProducts(PRODUCT_IMAGES_DIR, file, index);
                    imageFileNames.add(imageFileName);
                    index++;
                } catch (IOException e) {
                    throw new BadRequestException("Không thể tải lên hình ảnh sản phẩm: " + e.getMessage());
                }
            }
            existingProduct.setImages(imageFileNames);
        }

        existingProduct = productRepository.save(existingProduct);
        return modelMapper.map(existingProduct, ProductDTO.class);
    }

    @Override
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Sản phẩm không tìm thấy với id: " + id);
        }
        productRepository.deleteById(id);
    }

    @Override
    public BaseResponse<ProductDTO> getProductIsNews(Pageable pageable) {
        Page<Product> products = productRepository.findByIsNewTrue(pageable);

        BaseResponse<ProductDTO> response = new BaseResponse<>();
        response.setContent(products.getContent().stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .collect(Collectors.toList()));
        response.setPageNumber(products.getNumber());
        response.setPageSize(products.getSize());
        response.setTotalElements(products.getTotalElements());
        response.setTotalPages(products.getTotalPages());
        response.setLastPage(products.isLast());

        return response;
    }

    @Override
    public BaseResponse<ProductDTO> getProductIsSales(Pageable pageable) {
        Page<Product> products = productRepository.findByIsSaleTrue(pageable);

        BaseResponse<ProductDTO> response = new BaseResponse<>();
        response.setContent(products.getContent().stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .collect(Collectors.toList()));
        response.setPageNumber(products.getNumber());
        response.setPageSize(products.getSize());
        response.setTotalElements(products.getTotalElements());
        response.setTotalPages(products.getTotalPages());
        response.setLastPage(products.isLast());

        return response;
    }

    @Override
    public BaseResponse<ProductDTO> findByCategoryCategoryId(Long categoryId, Pageable pageable) {
        Page<Product> products = productRepository.findByCategoryId(categoryId, pageable);

        BaseResponse<ProductDTO> response = new BaseResponse<>();
        response.setContent(products.getContent().stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .collect(Collectors.toList()));
        response.setPageNumber(products.getNumber());
        response.setPageSize(products.getSize());
        response.setTotalElements(products.getTotalElements());
        response.setTotalPages(products.getTotalPages());
        response.setLastPage(products.isLast());

        return response;
    }

    @Override
    public BaseResponse<ProductDTO> getProductBySearch(String search, Pageable pageable) {
        if (search == null || search.trim().isEmpty()) {
            throw new BadRequestException("Chuỗi tìm kiếm không được để trống");
        }

        Page<Product> products = productRepository.findByNameContainingIgnoreCase(search, pageable);

        BaseResponse<ProductDTO> response = new BaseResponse<>();
        response.setContent(products.getContent().stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .collect(Collectors.toList()));
        response.setPageNumber(products.getNumber());
        response.setPageSize(products.getSize());
        response.setTotalElements(products.getTotalElements());
        response.setTotalPages(products.getTotalPages());
        response.setLastPage(products.isLast());

        return response;
    }

    @Override
    public InputStream getProductImage(String fileName) throws FileNotFoundException {
        return fileService.getResource(PRODUCT_IMAGE_DIR, fileName);
    }

    @Override
    public InputStream getProductImages(String fileNames) throws FileNotFoundException {
        return fileService.getResource(PRODUCT_IMAGES_DIR, fileNames);
    }
}