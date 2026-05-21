package ru.practicum.model.compilation;

import ru.practicum.model.event.Event;

public record EventCompilationId(Long compilationId, Event event) {
}
