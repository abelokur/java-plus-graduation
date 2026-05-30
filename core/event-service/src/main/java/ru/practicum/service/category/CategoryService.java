package ru.practicum.service.category;

import org.springframework.data.domain.Pageable;
import ru.practicum.dto.category.CategoryDto;

import java.util.List;

public interface CategoryService {
    CategoryDto save(CategoryDto category);

    void delete(Long id);

    CategoryDto update(Long id, CategoryDto category);

    CategoryDto findById(Long id);

    List<CategoryDto> findAll(Pageable pageable);
}
