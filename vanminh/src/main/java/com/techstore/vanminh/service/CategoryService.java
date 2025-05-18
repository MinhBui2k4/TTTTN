package com.techstore.vanminh.service;

import com.techstore.vanminh.dto.CategoryDTO;
import com.techstore.vanminh.dto.response.BaseResponse;

public interface CategoryService {
    CategoryDTO createCategory(CategoryDTO categoryDTO);

    CategoryDTO getCategoryById(Long id);

    BaseResponse<CategoryDTO> getAllCategories(int pageNumber, int pageSize, String sortBy, String sortOrder);

    CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO);

    void deleteCategory(Long id);
}
