package br.com.resilience.controller;

import br.com.resilience.service.ExternalAPICallerService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.resilience4j.timelimiter.annotation.TimeLimiter;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/")
public class ResilientAppController {

    private final ExternalAPICallerService externalAPICallerService;

    @Autowired
    public ResilientAppController(ExternalAPICallerService externalAPICallerService) {
        this.externalAPICallerService = externalAPICallerService;
    }

    @GetMapping("/circuit-breaker")
    public ResponseEntity<String> circuitBreakerApi() {
        return ResponseEntity.ok(externalAPICallerService.getCircuitBreaker());
    }

    @GetMapping("/retry")
    public ResponseEntity<String> retryApi() {
        return ResponseEntity.ok(externalAPICallerService.getRetry());

    }

    @GetMapping("/time-limiter-async")
    public CompletableFuture<String> timeLimiterApiAsync() {
        return externalAPICallerService.callApiWithDelayAsync();
    }

    @GetMapping("/time-limiter")
    @TimeLimiter(name = "timeLimiterApi")
    public CompletableFuture<String> timeLimiterApi() {
        return CompletableFuture.supplyAsync(
                externalAPICallerService::callApiWithDelay);
    }

    @GetMapping("/bulkhead")
    public ResponseEntity<String> bulkheadApi() {
        return externalAPICallerService.callBulkhead();
    }

    @GetMapping("/rate-limiter")
    public String rateLimitApi() {
        return externalAPICallerService.callRateLimiter();
    }

//    public String fallbackAfterRetry(Exception ex) {
//        return "all retries have exhausted";
//    }

}