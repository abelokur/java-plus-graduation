package ru.practicum.repository;

import com.querydsl.core.BooleanBuilder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ru.practicum.model.comment.Comment;
import ru.practicum.model.comment.CommentState;
import ru.practicum.model.comment.QComment;
import ru.practicum.model.user.User;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long>, QuerydslPredicateExecutor<Comment> {
    List<Comment> findAllByAuthor(User author);

    interface Predicate {
        static BooleanBuilder textFilter(String text) {
            BooleanBuilder predicate = new BooleanBuilder();
            QComment comment = QComment.comment;

            if (text != null && !text.isBlank()) {
                String searchText = text.trim();
                predicate.and(comment.text.lower().contains(searchText.toLowerCase()));
            }

            return predicate;
        }

        static BooleanBuilder stateFilter(CommentState state) {
            BooleanBuilder predicate = new BooleanBuilder();
            QComment comment = QComment.comment;
            predicate.and(comment.state.eq(state));
            return predicate;
        }

        static BooleanBuilder eventFilter(long eventId) {
            BooleanBuilder predicate = new BooleanBuilder();
            QComment comment = QComment.comment;
            predicate.and(comment.event.id.eq(eventId));
            return predicate;
        }
    }
}
