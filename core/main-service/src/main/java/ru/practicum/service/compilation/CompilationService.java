package ru.practicum.service.compilation;

import org.springframework.data.domain.Pageable;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {
    CompilationDto createCompilation(NewCompilationDto compilationDto);

    void deleteCompilation(long compilationId);

    CompilationDto updateCompilation(long compilationId, UpdateCompilationRequest compilationDto);

    List<CompilationDto> findCompilationsByParam(Boolean pinned, Pageable pageable);

    CompilationDto findCompilationById(long compilationId);
}
