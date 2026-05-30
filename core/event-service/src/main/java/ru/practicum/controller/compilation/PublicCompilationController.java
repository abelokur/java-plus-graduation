package ru.practicum.controller.compilation;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import ru.practicum.annotation.LogAllMethods;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.service.compilation.CompilationService;
import ru.practicum.util.OffsetBasedPageable;

import java.util.List;

@RestController
@RequestMapping("/compilations")
@Slf4j
@RequiredArgsConstructor
@LogAllMethods
public class PublicCompilationController {
    private final CompilationService compilationService;

    @GetMapping
    public List<CompilationDto> getAllCompilations(@RequestParam(required = false, name = "pinned") Boolean pinned,
                                                   @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                                   @RequestParam(defaultValue = "10") @Positive Integer size) {
       Pageable pageable = new OffsetBasedPageable(from, size);
        return compilationService.findCompilationsByParam(pinned, pageable);
    }

    @GetMapping("/{compId}")
    public CompilationDto getCompilationById(@PathVariable("compId") Long compilationId) {
        return compilationService.findCompilationById(compilationId);
    }
}
