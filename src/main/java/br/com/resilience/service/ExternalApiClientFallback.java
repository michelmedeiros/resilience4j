package br.com.resilience.service;

import br.com.resilience.client.ExternalApiClient;
import br.com.resilience.entity.Book;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.circuitbreaker.NoFallbackAvailableException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

@Slf4j
public class ExternalApiClientFallback implements ExternalApiClient {

    private final Exception cause;

    public ExternalApiClientFallback(Exception cause) {
        this.cause = cause;
    }

    @Override
    public String getCircuitBreaker() {
        log.error("Fallback getCircuitBreaker with error Cause: {}, Message: {}", cause, cause.getMessage());
        throw new NoFallbackAvailableException("Boom!", new RuntimeException());
    }

    @Override
    public List<Book> getAllBooks() {
        log.error("Fallback getAllBooks with error Cause: {}, Message: {}", cause, cause.getMessage());
        return new ArrayList<>();
    }

    @Override
    public String getRetry() {
        throw new NoFallbackAvailableException("Retry!", new RuntimeException());
    }

    @Override
    public String getTimeLimiterApi() {
//        return "ok";
        throw new NoFallbackAvailableException("Timeout!", new TimeoutException());
    }

    @Override
    public String getBulkhead() {
        return "ok";
    }

    @Override
    public String getRateLimiter() {
//        log.error("Fallback getRateLimiter with error Cause: {}, Message: {}", cause, cause.getMessage());
//        throw new NoFallbackAvailableException("Rate Limiter!", new RuntimeException());
        return "ok";
    }
}
