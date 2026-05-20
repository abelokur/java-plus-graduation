package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import ru.practicum.model.category.Category;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long>, QuerydslPredicateExecutor<Category> {
    @Query(
            "select case when count(c) > 0 then true else false end " +
            "from Category c where lower(trim(c.name)) = lower(trim(:name))"
    )
    boolean existsByNameIgnoreCaseAndTrim(@Param("name") String name);

    @Query(value = "SELECT * FROM categories ORDER BY id LIMIT :size OFFSET :from", nativeQuery = true)
    List<Category> findAllWithOffset(@Param("from") int from,
                                     @Param("size") int size);
}
