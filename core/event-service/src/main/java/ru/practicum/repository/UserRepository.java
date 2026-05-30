package ru.practicum.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ru.practicum.model.user.QUser;
import ru.practicum.model.user.User;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long>, QuerydslPredicateExecutor<User> {

    interface Predicate {
        static BooleanExpression byIds(List<Long> ids) {
            ru.practicum.model.user.QUser user = QUser.user;
            return user.id.in(ids);
        }
    }
}