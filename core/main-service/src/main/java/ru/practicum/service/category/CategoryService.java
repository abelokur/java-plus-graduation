package ru.practicum.service.category;

import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.CategoryParam;

import java.util.List;

public interface CategoryService {
    CategoryDto save(CategoryDto category);

    void delete(Long id);

    CategoryDto update(Long id, CategoryDto category);

    CategoryDto findById(Long id);

    List<CategoryDto> findAll(CategoryParam params);
}
