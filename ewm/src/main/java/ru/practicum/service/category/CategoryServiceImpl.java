package ru.practicum.service.category;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.CategoryParam;
import ru.practicum.exception.AlreadyExistsException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.model.category.Category;
import ru.practicum.model.category.mapper.CategoryMapper;
import ru.practicum.repository.CategoryRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public CategoryDto save(CategoryDto category) {
        Category saveCat;
        if (categoryRepository.existsByNameIgnoreCaseAndTrim(category.name())) {
            throw new AlreadyExistsException("Категория с названием " + category.name() + " уже существует");
        }
        saveCat = categoryRepository.save(CategoryMapper.mapToCategory(category));

        return CategoryMapper.mapToCategoryDto(saveCat);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new NotFoundException("Категория с id " + id + " не найдена");
        }

        categoryRepository.deleteById(id);
    }

    @Override
    @Transactional
    public CategoryDto update(Long id, CategoryDto category) {
        String catDtoName = category.name();
        Category getCat = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Категория с id " + id + " не найдена"));

        if (
                categoryRepository.existsByNameIgnoreCaseAndTrim(catDtoName) &&
                !getCat.getName().equalsIgnoreCase(catDtoName)
        ) {
            throw new AlreadyExistsException("Категория с названием " + catDtoName + " уже существует");
        }

        getCat.setName(catDtoName);

        return CategoryMapper.mapToCategoryDto(categoryRepository.save(getCat));
    }

    @Override
    public CategoryDto findById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Категория с id " + id + " не найдена"));
        return CategoryMapper.mapToCategoryDto(category);
    }

    @Override
    public List<CategoryDto> findAll(CategoryParam params) {
        List<Category> categories = categoryRepository.findAllWithOffset(params.from(), params.size());

        return categories.stream()
                .map(CategoryMapper::mapToCategoryDto)
                .toList();
    }
}
