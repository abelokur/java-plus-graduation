package ru.practicum.model.category.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.model.category.Category;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CategoryMapper {
    CategoryDto toDto(Category category);

    List<CategoryDto> toDto(List<Category> categories);

    Category toEntity(CategoryDto categoryDto);
}
