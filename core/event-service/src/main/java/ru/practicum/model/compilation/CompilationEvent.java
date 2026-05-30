package ru.practicum.model.compilation;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.model.event.Event;

@Entity
@Table(name = "compilation_events")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompilationEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "compilation_id")
    private Compilation compilation;

    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;
}
