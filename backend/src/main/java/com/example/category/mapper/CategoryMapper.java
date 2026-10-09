package com.example.category.mapper;

import com.example.category.dto.CategoryResponse;
import com.example.category.model.Category;
import com.example.config.MappingConfig;
import java.util.List;

import org.mapstruct.Mapper;

/**
 * 
 * <b>CategoryMapper</b>
 * <br>
 * Mapper interface responsible for mapping Category objects to DTOs
 */
@Mapper(
    config = MappingConfig.class
)
public interface CategoryMapper {

    CategoryResponse toResponse(Category category);
    List<CategoryResponse> toResponseList(List<Category> categories);
}
