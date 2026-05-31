package ru.practicum.service.category;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.exception.AlreadyExistsException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.model.category.Category;
import ru.practicum.model.category.mapper.CategoryMapper;
import ru.practicum.repository.CategoryRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    @CacheEvict(cacheNames = "categories", allEntries = true)
    public CategoryDto save(CategoryDto category) {
        Category saveCat;

        if (categoryRepository.existsByNameIgnoreCaseAndTrim(category.name())) {
            throw new AlreadyExistsException("Категория с названием " + category.name() + " уже существует");
        }

        saveCat = categoryRepository.save(categoryMapper.toEntity(category));

        return categoryMapper.toDto(saveCat);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "categories", allEntries = true)
    public void delete(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new NotFoundException("Категория с id " + id + " не найдена");
        }

        categoryRepository.deleteById(id);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "categories", allEntries = true)
    public CategoryDto update(Long id, CategoryDto category) {
        String catDtoName = category.name();

        Category getCat = getCategory(id);

        if (
                categoryRepository.existsByNameIgnoreCaseAndTrim(catDtoName) &&
                !getCat.getName().equalsIgnoreCase(catDtoName)
        ) {
            throw new AlreadyExistsException("Категория с названием " + catDtoName + " уже существует");
        }

        getCat.setName(catDtoName);

        return categoryMapper.toDto(categoryRepository.save(getCat));
    }

    @Override
    @Cacheable(cacheNames = "categories")
    public CategoryDto findById(Long id) {
        return categoryMapper.toDto(getCategory(id));
    }

    @Override
    @Cacheable(cacheNames = "categories")
    public List<CategoryDto> findAll(Pageable pageable) {
        List<Category> categories = categoryRepository.findAll(pageable).toList();
        return categoryMapper.toDto(categories);
    }

    private Category getCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Категория с id " + id + " не найдена"));
    }
}
