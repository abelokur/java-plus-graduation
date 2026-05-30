package ru.practicum.model.request;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.model.RequestStatus;
import ru.practicum.model.event.Event;

import java.time.LocalDateTime;

@Entity
@Table(name = "requests")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Request {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "requester_id", nullable = false)
    private Long requesterId;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RequestStatus status;

    @Column(name = "created_on", nullable = false)
    private LocalDateTime createdOn;
}
