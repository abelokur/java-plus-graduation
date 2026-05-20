package ru.practicum.repository;

import com.querydsl.core.BooleanBuilder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ru.practicum.model.compilation.Compilation;
import ru.practicum.model.compilation.QCompilation;

import com.querydsl.core.types.Predicate;

public interface CompilationRepository extends JpaRepository<Compilation, Long>, QuerydslPredicateExecutor<Compilation> {

    interface Predicates {
        static Predicate buildPredicates(Boolean pinned) {
            BooleanBuilder bb = new BooleanBuilder();

            if (pinned != null) {
                bb.and(QCompilation.compilation.pinned.eq(pinned));
            }

            return bb;
        }
    }
}
