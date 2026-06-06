package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AnalyzerRunner implements CommandLineRunner {
    final InteractionProcessor interactionProcessor;
    final SimilarityProcessor similarityProcessor;

    @Override
    public void run(String... args) throws Exception {
        Thread interactionThread = new Thread(interactionProcessor);
        interactionThread.setName("InteractionProcessorThread");
        interactionThread.start();

        Thread similarityThread = new Thread(similarityProcessor);
        similarityThread.setName("SimilarityProcessorThread");
        similarityThread.start();
    }
}

