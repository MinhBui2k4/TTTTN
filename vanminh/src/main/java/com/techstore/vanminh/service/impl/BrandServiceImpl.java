package com.techstore.vanminh.service.impl;

import com.techstore.vanminh.dto.BrandDTO;
import com.techstore.vanminh.entity.Brand;
import com.techstore.vanminh.exception.BadRequestException;
import com.techstore.vanminh.exception.ResourceNotFoundException;
import com.techstore.vanminh.repository.BrandRepository;
import com.techstore.vanminh.service.BrandService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BrandServiceImpl implements BrandService {

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public BrandDTO createBrand(BrandDTO brandDTO) {
        if (brandRepository.findByName(brandDTO.getName()).isPresent()) {
            throw new BadRequestException("Thương hiệu đã tồn tại: " + brandDTO.getName());
        }
        Brand brand = modelMapper.map(brandDTO, Brand.class);
        brand = brandRepository.save(brand);
        return modelMapper.map(brand, BrandDTO.class);
    }

    @Override
    public BrandDTO getBrandById(Long id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Thương hiệu không tìm thấy với id: " + id));
        return modelMapper.map(brand, BrandDTO.class);
    }

    @Override
    public Page<BrandDTO> getAllBrands(Pageable pageable) {
        Page<Brand> brands = brandRepository.findAll(pageable);
        return brands.map(brand -> modelMapper.map(brand, BrandDTO.class));
    }

    @Override
    public BrandDTO updateBrand(Long id, BrandDTO brandDTO) {
        Brand existingBrand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Thương hiệu không tìm thấy với id: " + id));

        if (!existingBrand.getName().equals(brandDTO.getName()) &&
                brandRepository.findByName(brandDTO.getName()).isPresent()) {
            throw new BadRequestException("Thương hiệu đã tồn tại: " + brandDTO.getName());
        }

        modelMapper.map(brandDTO, existingBrand);
        existingBrand = brandRepository.save(existingBrand);
        return modelMapper.map(existingBrand, BrandDTO.class);
    }

    @Override
    public void deleteBrand(Long id) {
        if (!brandRepository.existsById(id)) {
            throw new ResourceNotFoundException("Thương hiệu không tìm thấy với id: " + id);
        }
        brandRepository.deleteById(id);
    }
}
