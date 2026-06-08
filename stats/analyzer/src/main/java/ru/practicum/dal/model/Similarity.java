package ru.practicum.dal.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;

import java.time.Instant;

@Entity
@Table(name = "similarities")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@DynamicUpdate
public class Similarity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "event1")
    private Long event1;

    @Column(name = "event2")
    private Long event2;

    @Column(name = "similarity")
    private Double similarity;

    @Column(name = "ts")
    private Instant createdAt;
}
