package ewm.client.stats;

public interface CollectorClient {
    void saveView(long userId, long eventId);

    void saveLike(long userId, long eventId);

    void saveRegister(long userId, long eventId);
}
