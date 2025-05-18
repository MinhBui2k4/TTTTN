package com.techstore.vanminh.service.impl;

import com.techstore.vanminh.dto.CategoryDTO;
import com.techstore.vanminh.dto.response.BaseResponse;
import com.techstore.vanminh.entity.Category;
import com.techstore.vanminh.exception.BadRequestException;
import com.techstore.vanminh.exception.ResourceNotFoundException;
import com.techstore.vanminh.repository.CategoryRepository;
import com.techstore.vanminh.service.CategoryService;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        if (categoryRepository.findByName(categoryDTO.getName()).isPresent()) {
            throw new BadRequestException("Danh mục đã tồn tại: " + categoryDTO.getName());
        }

        Category category = modelMapper.map(categoryDTO, Category.class);
        category = categoryRepository.save(category);
        return modelMapper.map(category, CategoryDTO.class);
    }

    @Override
    public CategoryDTO getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục không tìm thấy với id: " + id));
        return modelMapper.map(category, CategoryDTO.class);
    }

    @Override
    public BaseResponse<CategoryDTO> getAllCategories(int pageNumber, int pageSize, String sortBy, String sortOrder) {
        Sort sort = sortOrder.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        Page<Category> categoryPage = categoryRepository.findAll(pageable);

        List<CategoryDTO> categoryDTOs = categoryPage.getContent().stream()
                .map(category -> modelMapper.map(category, CategoryDTO.class))
                .collect(Collectors.toList());

        BaseResponse<CategoryDTO> response = new BaseResponse<>();
        response.setContent(categoryDTOs);
        response.setPageNumber(categoryPage.getNumber());
        response.setPageSize(categoryPage.getSize());
        response.setTotalElements(categoryPage.getTotalElements());
        response.setTotalPages(categoryPage.getTotalPages());
        response.setLastPage(categoryPage.isLast());

        return response;
    }

    @Override
    public CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO) {
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục không tìm thấy với id: " + id));

        // Kiểm tra tên danh mục đã tồn tại (nếu đổi tên)
        if (!existingCategory.getName().equalsIgnoreCase(categoryDTO.getName()) &&
                categoryRepository.findByName(categoryDTO.getName()).isPresent()) {
            throw new BadRequestException("Danh mục đã tồn tại: " + categoryDTO.getName());
        }

        existingCategory.setName(categoryDTO.getName());
        existingCategory.setDescription(categoryDTO.getDescription());

        Category updatedCategory = categoryRepository.save(existingCategory);
        return modelMapper.map(updatedCategory, CategoryDTO.class);
    }

    @Override
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Danh mục không tìm thấy với id: " + id);
        }
        categoryRepository.deleteById(id);
    }
}
