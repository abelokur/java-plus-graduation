package ewm.client.stats;

import ru.practicum.ewm.stats.proto.ActionTypeProto;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;

import java.util.List;
import java.util.stream.Stream;

public interface RecommendationsClient {

    Stream<RecommendedEventProto> getRecommendationsForUser(long userId, int maxResults, ActionTypeProto action);

    Stream<RecommendedEventProto> getSimilarEvents(long eventId, long userId, int maxResults);

    Stream<RecommendedEventProto> GetInteractionsCount(List<Long> eventIds);
}
