package ru.practicum.controller;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.dal.service.RecommendationService;
import ru.practicum.ewm.stats.proto.*;

import java.util.List;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class RecommendationController extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {
    private final RecommendationService recommendationService;

    @Override
    public void getRecommendationsForUser(UserPredictionsRequestProto request, StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            List<RecommendedEventProto> recommendations =
                    recommendationService.getRecommendationsForUser(request);

            recommendations.forEach(responseObserver::onNext);
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("Error getting recommendations", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void getSimilarEvents(SimilarEventsRequestProto request, StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            List<RecommendedEventProto> recommendations =
                    recommendationService.getSimilarEvents(request);

            recommendations.forEach(responseObserver::onNext);
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("Error getting recommendations", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void getInteractionsCount(InteractionsCountRequestProto request, StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            List<RecommendedEventProto> recommendations =
                    recommendationService.getInteractionsCount(request);

            recommendations.forEach(responseObserver::onNext);
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("Error getting recommendations", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        }
    }
}
