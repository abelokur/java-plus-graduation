package ewm.client.stats;

import com.google.protobuf.Timestamp;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.proto.ActionTypeProto;
import ru.practicum.ewm.stats.proto.UserActionControllerGrpc;
import ru.practicum.ewm.stats.proto.UserActionProto;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class CollectorClientDefault implements CollectorClient {
    @GrpcClient("collector")
    private UserActionControllerGrpc.UserActionControllerBlockingStub actionClient;

    public void saveView(long userId, long eventId) {
        saveUserInteraction(userId, eventId, ActionTypeProto.ACTION_VIEW);
    }

    public void saveLike(long userId, long eventId) {
        saveUserInteraction(userId, eventId, ActionTypeProto.ACTION_LIKE);
    }

    public void saveRegister(long userId, long eventId) {
        saveUserInteraction(userId, eventId, ActionTypeProto.ACTION_REGISTER);
    }


    @Retryable(
            retryFor = StatusRuntimeException.class,
            maxAttempts = 5,
            backoff = @Backoff(delay = 300))
    private void saveUserInteraction(long userId, long eventId, ActionTypeProto actionType) {
        Instant now = Instant.now();

        UserActionProto request = UserActionProto.newBuilder()
                .setUserId(userId)
                .setEventId(eventId)
                .setActionType(actionType)
                .setTimestamp(
                        Timestamp.newBuilder()
                                .setSeconds(now.getEpochSecond())
                                .setNanos(now.getNano())
                                .build()
                )
                .build();

        if (actionClient == null) {
            log.error("GRPC client is not initialized");
            return;
        }

        try {
            actionClient.collectUserAction(request);
            log.info("User interaction saved: {}", request);
        } catch (StatusRuntimeException e) {
            log.error("Error saving user interaction, status: {}, message: {}", e.getStatus(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error saving user interaction, message: {}", e.getMessage());
            throw e;
        }
    }

}
