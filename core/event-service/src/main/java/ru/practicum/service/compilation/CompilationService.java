package ru.practicum.service.compilation;

import org.springframework.data.domain.Pageable;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationRequest;
import ru.practicum.dto.compilation.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {
    CompilationDto createCompilation(NewCompilationRequest compilationDto);

    void deleteCompilation(Long compilationId);

    CompilationDto updateCompilation(Long compilationId, UpdateCompilationRequest compilationDto);

    List<CompilationDto> findCompilationsByParam(Boolean pinned, Pageable pageable);

    CompilationDto findCompilationById(Long compilationId);
}
