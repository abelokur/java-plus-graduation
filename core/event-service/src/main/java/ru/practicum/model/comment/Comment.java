package ru.practicum.model.comment;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import ru.practicum.model.CommentState;
import ru.practicum.model.event.Event;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "author_id")
    private Long authorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    @ToString.Exclude
    private Event event;

    @Column(nullable = false)
    private String text;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private CommentState state;

    @CreationTimestamp
    @Column(name = "created_on")
    private LocalDateTime created;
}