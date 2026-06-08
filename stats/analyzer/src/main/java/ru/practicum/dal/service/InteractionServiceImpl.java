package ru.practicum.dal.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.dal.model.Interaction;
import ru.practicum.dal.model.mapper.Mapper;
import ru.practicum.dal.repository.InteractionRepository;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InteractionServiceImpl implements InteractionService {
    private final InteractionRepository interactionRepository;
    private final Mapper mapper;

    @Transactional
    @Override
    public void saveOrUpdate(UserActionAvro userActionAvro) {
        Interaction newInteraction = mapper.toInteractionFromAvro(userActionAvro);
        Optional<Interaction> oldInteractionOpt = interactionRepository.getByUserIdAndEventId(
                userActionAvro.getUserId(), userActionAvro.getEventId());

        if (oldInteractionOpt.isPresent()) {
            Interaction oldInteraction = oldInteractionOpt.get();
            if (newInteraction.getRating() > oldInteraction.getRating()) {
                oldInteraction.setRating(newInteraction.getRating());
                oldInteraction.setCreatedAt(newInteraction.getCreatedAt());
                interactionRepository.save(oldInteraction);
            }
        } else {
            interactionRepository.save(newInteraction);
        }
    }
}
