package br.com.resilience.service;

import br.com.resilience.client.AsyncExternalApiClient;
import br.com.resilience.client.ExternalApiClient;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExternalAPICallerService {

    private final ExternalApiClient externalAPICaller;
    private final AsyncExternalApiClient asyncExternalApiClient;

    public String getCircuitBreaker() {
        return externalAPICaller.getCircuitBreaker();
    }

    @Retry(name = "retryApi", fallbackMethod = "fallbackAfterRetry")
    public String getRetry() {
        return externalAPICaller.getRetry();
    }

    private String fallbackAfterRetry(RuntimeException ex) {
        return "all retries have exhausted";
    }
    public String callApiWithDelay() {
        var timeLimiterApiComplete = externalAPICaller.getTimeLimiterApi();
        try {
            Thread.sleep(500);
        } catch (InterruptedException ignore) {
            log.error("Error: {}", ignore.getMessage());
            ignore.printStackTrace();
        }
        return timeLimiterApiComplete;
    }

    @TimeLimiter(name = "asyncTimeLimiterApi")
    public CompletableFuture<String> callApiWithDelayAsync() {
        return CompletableFuture.supplyAsync(
                asyncExternalApiClient::getAsyncTimeLimiterApi);
    }

    @Bulkhead(name = "bulkheadApi")
    public ResponseEntity<String> callBulkhead() {
        return ResponseEntity.ok(externalAPICaller.getBulkhead());
    }

    @RateLimiter(name = "rateLimiterApi")
    public String callRateLimiter() {
        return externalAPICaller.getRateLimiter();
    }
}