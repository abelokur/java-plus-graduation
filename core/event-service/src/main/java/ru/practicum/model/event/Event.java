package ru.practicum.model.event;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.model.EventState;
import ru.practicum.model.category.Category;

import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "initiator_id", nullable = false)
    private Long initiatorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @ToString.Exclude
    private Category category;

    @Column(name = "title", length = 120, nullable = false)
    private String title;

    @Column(name = "annotation", length = 2000, nullable = false)
    private String annotation;

    @Column(name = "description", length = 7000, nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", length = 20, nullable = false)
    private EventState state;

    @Embedded
    private Location location;

    @Column(name = "participant_limit", nullable = false)
    private Integer participantLimit;

    @Column(name = "request_moderation", nullable = false)
    private Boolean requestModeration;

    @Column(name = "paid", nullable = false)
    private Boolean paid;

    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;

    @Column(name = "published_on")
    private LocalDateTime publishedOn;

    @Column(name = "created_on", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdOn = LocalDateTime.now();

    @Transient
    private Double rating;

    @Column(name = "confirmed_requests")
    private Long confirmedRequests;
}
