package com.techstore.vanminh.service;

import com.techstore.vanminh.dto.BrandDTO;
import com.techstore.vanminh.dto.response.BaseResponse;

public interface BrandService {
    BrandDTO createBrand(BrandDTO brandDTO);

    BrandDTO getBrandById(Long id);

    BaseResponse<BrandDTO> getAllBrands(int pageNumber, int pageSize, String sortBy, String sortOrder);

    BrandDTO updateBrand(Long id, BrandDTO brandDTO);

    void deleteBrand(Long id);
}
