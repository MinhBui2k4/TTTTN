package com.techstore.vanminh.service;

import com.techstore.vanminh.dto.BrandDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BrandService {
    BrandDTO createBrand(BrandDTO brandDTO);

    BrandDTO getBrandById(Long id);

    Page<BrandDTO> getAllBrands(Pageable pageable);

    BrandDTO updateBrand(Long id, BrandDTO brandDTO);

    void deleteBrand(Long id);
}
