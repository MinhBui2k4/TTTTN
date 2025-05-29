package com.techstore.vanminh.util;

import com.techstore.vanminh.dto.ProductDTO;
import com.techstore.vanminh.entity.Product;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Component
public class ProductMapper {

    private static final Logger logger = Logger.getLogger(ProductMapper.class.getName());

    public ProductDTO toProductDTO(Product product) {
        logger.info("Mapping Product to ProductDTO: " + product.getId());
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setOldPrice(product.getOldPrice());
        dto.setRating(product.getRating());
        dto.setImage(product.getImage());
        dto.setImages(product.getImages());
        dto.setNew(product.isNew());
        dto.setSale(product.isSale());
        dto.setQuantity(product.getQuantity());
        dto.setReview(product.getReview());
        dto.setCategoryId(product.getCategory() != null ? product.getCategory().getId() : null);
        dto.setBrandId(product.getBrand() != null ? product.getBrand().getId() : null);
        dto.setSku(product.getSku());
        dto.setAvailability(product.isAvailability());
        logger.info("Completed mapping Product to ProductDTO: " + product.getId());
        return dto;
    }
}