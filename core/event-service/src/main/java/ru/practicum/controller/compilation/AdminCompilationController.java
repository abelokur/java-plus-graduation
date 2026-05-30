package ru.practicum.controller.compilation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.annotation.LogAllMethods;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationRequest;
import ru.practicum.dto.compilation.UpdateCompilationRequest;
import ru.practicum.service.compilation.CompilationService;

@RestController
@RequestMapping("/admin/compilations")
@Slf4j
@RequiredArgsConstructor
@LogAllMethods
public class AdminCompilationController {
    private final CompilationService compilationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto createCompilation(@RequestBody @Valid NewCompilationRequest compilationDto) {
        return compilationService.createCompilation(compilationDto);
    }

    @DeleteMapping("/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompilation(@PathVariable("compId") Long compilationId) {
        compilationService.deleteCompilation(compilationId);
    }

    @PatchMapping("/{compId}")
    public CompilationDto updateCompilation(@RequestBody @Valid UpdateCompilationRequest compilationUpdateDto,
                                            @PathVariable("compId") Long compilationId) {
        return compilationService.updateCompilation(compilationId, compilationUpdateDto);
    }
}
