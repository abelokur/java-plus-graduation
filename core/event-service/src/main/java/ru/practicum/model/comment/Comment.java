package ru.practicum.model.comment;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.model.event.Event;
import ru.practicum.model.user.User;

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
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    @ToString.Exclude
    private User author;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    @ToString.Exclude
    private Event event;
    @Column(nullable = false)
    private String text;
    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private CommentState state;
    @Column(name = "created_on")
    private LocalDateTime created;
}