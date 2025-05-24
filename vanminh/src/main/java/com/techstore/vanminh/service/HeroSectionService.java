package com.techstore.vanminh.service;

import com.techstore.vanminh.dto.HeroSectionDTO;
import com.techstore.vanminh.dto.response.BaseResponse;

import java.io.FileNotFoundException;
import java.io.InputStream;

public interface HeroSectionService {
    HeroSectionDTO createHeroSection(HeroSectionDTO dto);

    BaseResponse<HeroSectionDTO> getAllHeroSections(int pageNumber, int pageSize);

    HeroSectionDTO getHeroSectionById(Long id);

    HeroSectionDTO updateHeroSection(Long id, HeroSectionDTO dto);

    void deleteHeroSection(Long id);

    InputStream getHeroImage(String fileName) throws FileNotFoundException;
}
