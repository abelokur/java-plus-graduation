package ewm.client.stats;

import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.proto.*;

import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationsClientDefault implements RecommendationsClient {
    @GrpcClient("analyzer")
    private RecommendationsControllerGrpc.RecommendationsControllerBlockingStub client;

    @Retryable(
            retryFor = StatusRuntimeException.class,
            maxAttempts = 5,
            backoff = @Backoff(delay = 300))
    @Override
    public Stream<RecommendedEventProto> getRecommendationsForUser(long userId, int maxResults, ActionTypeProto action) {
        UserPredictionsRequestProto request = UserPredictionsRequestProto.newBuilder()
                .setUserId(userId)
                .setMaxResults(maxResults)
                .build();

        try {
            log.info("Getting recommendations for user [{}]", userId);
            return asStream(client.getRecommendationsForUser(request));
        } catch (StatusRuntimeException e) {
            log.error("Error getting recommendations for user [{}], status: {}, message: {}",
                    userId, e.getStatus(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error getting recommendations for user [{}], message: {}", userId, e.getMessage());
            throw e;
        }
    }

    @Retryable(
            retryFor = StatusRuntimeException.class,
            maxAttempts = 5,
            backoff = @Backoff(delay = 300))
    @Override
    public Stream<RecommendedEventProto> getSimilarEvents(long eventId, long userId, int maxResults) {
        SimilarEventsRequestProto request = SimilarEventsRequestProto.newBuilder()
                .setEventId(eventId)
                .setUserId(userId)
                .setMaxResults(maxResults)
                .build();

        try {
            log.info("Getting similar events for event [{}], user [{}]", eventId, userId);
            return asStream(client.getSimilarEvents(request));
        } catch (StatusRuntimeException e) {
            log.error("Error getting similar events for event [{}], status: {}, message: {}",
                    eventId, e.getStatus(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error getting similar events for event [{}], message: {}", eventId, e.getMessage());
            throw e;
        }
    }

    @Retryable(
            retryFor = StatusRuntimeException.class,
            maxAttempts = 5,
            backoff = @Backoff(delay = 300))
    @Override
    public Stream<RecommendedEventProto> GetInteractionsCount(List<Long> eventIds) {
        InteractionsCountRequestProto request = InteractionsCountRequestProto.newBuilder()
                .addAllEventId(eventIds)
                .build();

        try {
            log.info("Getting interactions count for events [{}]", eventIds);
            return asStream(client.getInteractionsCount(request));
        } catch (StatusRuntimeException e) {
            log.error("Error getting similar events for events [{}], status: {}, message: {}",
                    eventIds, e.getStatus(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error getting similar events for events [{}], message: {}", eventIds, e.getMessage());
            throw e;
        }
    }

    private Stream<RecommendedEventProto> asStream(Iterator<RecommendedEventProto> iterator) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED),
                false
        );
    }

}
