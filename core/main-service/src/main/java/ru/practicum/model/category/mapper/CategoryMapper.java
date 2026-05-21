package ru.practicum.model.category.mapper;

import ru.practicum.dto.category.CategoryDto;
import ru.practicum.model.category.Category;

public class CategoryMapper {
    public static Category mapToCategory(CategoryDto dto) {
        return Category.builder()
                .name(dto.name())
                .build();
    }

    public static CategoryDto mapToCategoryDto(Category category) {
        return new CategoryDto(category.getId(), category.getName());
    }
}
