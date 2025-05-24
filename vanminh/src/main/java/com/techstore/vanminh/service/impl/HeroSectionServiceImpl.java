package com.techstore.vanminh.service.impl;

import com.techstore.vanminh.dto.HeroSectionDTO;
import com.techstore.vanminh.dto.response.BaseResponse;
import com.techstore.vanminh.entity.HeroSection;
import com.techstore.vanminh.entity.User;
import com.techstore.vanminh.exception.BadRequestException;
import com.techstore.vanminh.exception.ResourceNotFoundException;
import com.techstore.vanminh.repository.HeroSectionRepository;
import com.techstore.vanminh.repository.UserRepository;
import com.techstore.vanminh.service.FileService;
import com.techstore.vanminh.service.HeroSectionService;
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
public class HeroSectionServiceImpl implements HeroSectionService {

    @Autowired
    private HeroSectionRepository heroSectionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileService fileService;

    @Autowired
    private ModelMapper modelMapper;

    private static final String HERO_IMAGE_DIR = "uploads/hero";

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tìm thấy"));
    }

    @Override
    public HeroSectionDTO createHeroSection(HeroSectionDTO dto) {
        User user = getCurrentUser();
        HeroSection entity = modelMapper.map(dto, HeroSection.class);
        entity.setCreatedBy(user);

        if (dto.getBackgroundImageFile() != null && !dto.getBackgroundImageFile().isEmpty()) {
            try {
                String fileName = fileService.uploadImgHeroSection(HERO_IMAGE_DIR, dto.getBackgroundImageFile());
                entity.setBackgroundImage(fileName);
            } catch (IOException e) {
                throw new BadRequestException("Không thể tải lên hình ảnh: " + e.getMessage());
            }
        }

        entity = heroSectionRepository.save(entity);
        return modelMapper.map(entity, HeroSectionDTO.class);
    }

    @Override
    public BaseResponse<HeroSectionDTO> getAllHeroSections(int pageNumber, int pageSize) {
        List<HeroSection> all = heroSectionRepository.findAll();

        int totalElements = all.size();
        int totalPages = (int) Math.ceil((double) totalElements / pageSize);
        int fromIndex = Math.min(pageNumber * pageSize, totalElements);
        int toIndex = Math.min(fromIndex + pageSize, totalElements);

        List<HeroSectionDTO> content = all.subList(fromIndex, toIndex).stream()
                .map(item -> modelMapper.map(item, HeroSectionDTO.class))
                .collect(Collectors.toList());

        BaseResponse<HeroSectionDTO> response = new BaseResponse<>();
        response.setContent(content);
        response.setPageNumber(pageNumber);
        response.setPageSize(pageSize);
        response.setTotalPages(totalPages);
        response.setLastPage(pageNumber >= totalPages - 1);
        return response;
    }

    @Override
    public HeroSectionDTO getHeroSectionById(Long id) {
        HeroSection entity = heroSectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy HeroSection với id: " + id));
        return modelMapper.map(entity, HeroSectionDTO.class);
    }

    @Override
    public HeroSectionDTO updateHeroSection(Long id, HeroSectionDTO dto) {
        HeroSection entity = heroSectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy HeroSection với id: " + id));

        modelMapper.map(dto, entity);

        if (dto.getBackgroundImageFile() != null && !dto.getBackgroundImageFile().isEmpty()) {
            try {
                String fileName = fileService.uploadImgHeroSection(HERO_IMAGE_DIR, dto.getBackgroundImageFile());
                entity.setBackgroundImage(fileName);
            } catch (IOException e) {
                throw new BadRequestException("Không thể tải lên hình ảnh: " + e.getMessage());
            }
        }

        entity = heroSectionRepository.save(entity);
        return modelMapper.map(entity, HeroSectionDTO.class);
    }

    @Override
    public void deleteHeroSection(Long id) {
        HeroSection entity = heroSectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy HeroSection với id: " + id));
        heroSectionRepository.delete(entity);
    }

    @Override
    public InputStream getHeroImage(String fileName) throws FileNotFoundException {
        return fileService.getResource(HERO_IMAGE_DIR, fileName);
    }
}
