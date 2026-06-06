package ru.practicum.dal.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.dal.model.Similarity;
import ru.practicum.dal.model.mapper.Mapper;
import ru.practicum.dal.repository.SimilarityRepository;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SimilarityServiceImpl implements SimilarityService {
    private final SimilarityRepository repository;
    private final Mapper mapper;

    @Transactional
    @Override
    public void saveOrUpdate(EventSimilarityAvro eventSimilarityAvro) {
        Similarity newSimilarity = mapper.toSimilarityFromAvro(eventSimilarityAvro);
        Optional<Similarity> oldSimilarityOpt = repository.getSimilarityByEvent1AndEvent2(
                eventSimilarityAvro.getEventA(), eventSimilarityAvro.getEventB());
        if (oldSimilarityOpt.isPresent()) {
            Similarity oldSimilarity = oldSimilarityOpt.get();
            oldSimilarity.setSimilarity(newSimilarity.getSimilarity());
            oldSimilarity.setCreatedAt(newSimilarity.getCreatedAt());
            repository.save(oldSimilarity);
        } else {
            repository.save(newSimilarity);
        }
    }
}
