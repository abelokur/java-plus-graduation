package ru.practicum.repository;

import com.querydsl.core.BooleanBuilder;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ru.practicum.dto.event.AdminEventParam;
import ru.practicum.dto.event.EventPublicParam;
import ru.practicum.model.EventState;
import ru.practicum.model.event.Event;
import ru.practicum.model.event.QEvent;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface EventRepository extends JpaRepository<Event, Long>, QuerydslPredicateExecutor<Event> {
    interface Predicate {
        /**
         * Фильтры для административного поиска событий
         */
        static BooleanBuilder adminFilters(AdminEventParam params) {
            BooleanBuilder predicate = new BooleanBuilder();

            addUsersPredicate(predicate, params.users());
            addStatesPredicate(predicate, params.states());
            addCategoriesPredicate(predicate, params.categories());
            addDatePredicate(predicate, params.rangeStart(), params.rangeEnd());

            return predicate;
        }

        /**
         * Фильтры для общедоступного поиска событий
         */
        static BooleanBuilder publicFilters(EventPublicParam params) {
            return publicFilters(params, null);
        }

        static BooleanBuilder publicFilters(EventPublicParam params, List<Long> availableIds) {
            BooleanBuilder predicate = new BooleanBuilder();

            addStatePredicate(predicate, EventState.PUBLISHED);
            addTextPredicate(predicate, params.text());
            addPaidPredicate(predicate, params.paid());
            addCategoriesPredicate(predicate, params.categories());
            addDatePredicate(predicate, params.rangeStart(), params.rangeEnd());
            addOnlyAvailablePredicate(predicate, params.onlyAvailable(), availableIds);

            return predicate;
        }

        /**
         * Добавляет фильтр по пользователям (инициаторам событий)
         */
        private static void addUsersPredicate(
                BooleanBuilder predicate,
                Set<Long> users
        ) {
            if (users != null && !users.isEmpty()) {
                predicate.and(QEvent.event.initiatorId.in(users));
            }
        }

        /**
         * Добавляет фильтр по множеству состояний событий
         */
        private static void addStatesPredicate(
                BooleanBuilder predicate,
                Set<EventState> states
        ) {
            if (states != null && !states.isEmpty()) {
                predicate.and(QEvent.event.state.in(states));
            }
        }

        /**
         * Добавляет фильтр по одиночному состоянию события
         */
        private static void addStatePredicate(
                BooleanBuilder predicate,
                EventState state
        ) {
            if (state != null) {
                predicate.and(QEvent.event.state.eq(state));
            }
        }

        /**
         * Добавляет текстовый поиск в annotation и description
         */
        private static void addTextPredicate(
                BooleanBuilder predicate,
                String text
        ) {
            if (text != null && !text.isBlank()) {
                String pattern = "%" + text.toLowerCase() + "%";
                predicate.and(QEvent.event.annotation.lower().like(pattern)
                        .or(QEvent.event.description.lower().like(pattern)));
            }
        }

        /**
         * Добавляет фильтр по категориям
         */
        private static void addCategoriesPredicate(
                BooleanBuilder predicate,
                Set<Long> categories
        ) {
            if (categories != null && !categories.isEmpty()) {
                predicate.and(QEvent.event.category.id.in(categories));
            }
        }

        /**
         * Добавляет фильтр по платности события
         */
        private static void addPaidPredicate(
                BooleanBuilder predicate,
                Boolean paid
        ) {
            if (paid != null) {
                predicate.and(QEvent.event.paid.eq(paid));
            }
        }

        /**
         * Добавляет фильтр по диапазону дат события
         */
        private static void addDatePredicate(
                BooleanBuilder predicate,
                LocalDateTime rangeStart,
                LocalDateTime rangeEnd
        ) {
            if (rangeStart != null && rangeEnd != null) {
                predicate.and(QEvent.event.eventDate.between(rangeStart, rangeEnd));
            } else if (rangeStart != null) {
                predicate.and(QEvent.event.eventDate.goe(rangeStart));
            } else if (rangeEnd != null) {
                predicate.and(QEvent.event.eventDate.loe(rangeEnd));
            } else {
                predicate.and(QEvent.event.eventDate.after(LocalDateTime.now()));
            }
        }

        /**
         * Добавляет фильтр по доступности события
         */
        private static void addOnlyAvailablePredicate(
                BooleanBuilder predicate,
                Boolean onlyAvailable,
                List<Long> availableIds
        ) {
            if (onlyAvailable != null && onlyAvailable && availableIds != null && !availableIds.isEmpty()) {
                predicate.and(QEvent.event.id.in(availableIds));
            }
        }

    }

    Optional<Event> findByIdAndState(Long eventId, EventState state);

    Optional<Event> findByIdAndInitiatorId(Long eventId, Long userId);

    List<Event> findAllByInitiatorId(Long userId, Pageable pageable);

    @Query("""
            SELECT e.id
            FROM Event e
            LEFT JOIN Request r ON r.event.id = e.id AND r.status = 'CONFIRMED'
            GROUP BY e.id, e.participantLimit
            HAVING e.participantLimit = 0 OR COUNT(r) < e.participantLimit
            """)
    List<Long> findEventIdsWithAvailableSlots();
}
