package ru.practicum.exception;

import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

/**
 * Базовый декодер ошибок Feign клиента.
 */
@Slf4j
public class BaseErrorDecoder implements ErrorDecoder {
    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        HttpStatus status = HttpStatus.valueOf(response.status());

        String message = buildErrorMessage(methodKey, status, response);
        logError(methodKey, status, response);

        if (status == HttpStatus.NOT_FOUND) {
            return new ResourceNotFoundException(message);
        }

        if (status.is5xxServerError()) {
            return new ServiceTemporaryUnavailableException(message);
        }

        if (status.is4xxClientError()) {
            return new BadRequestException(message);
        }

        return defaultErrorDecoder.decode(methodKey, response);
    }

    private String buildErrorMessage(String methodKey, HttpStatus status, Response response) {
        return String.format("Error in %s. Status: %d (%s). Path: %s",
                extractMethodName(methodKey),
                status.value(),
                status.getReasonPhrase(),
                response.request().url()
        );
    }

    private void logError(String methodKey, HttpStatus status, Response response) {
        if (status.is5xxServerError()) {
            log.error("Server error {} in method: {}, URL: {}",
                    status, methodKey, response.request().url());
        } else if (status.is4xxClientError()) {
            log.warn("Client error {} in method: {}, URL: {}",
                    status, methodKey, response.request().url());
        }
    }

    private String extractMethodName(String methodKey) {
        int hashIndex = methodKey.indexOf('#');
        if (hashIndex != -1) {
            return methodKey.substring(hashIndex + 1);
        }
        return methodKey;
    }
}