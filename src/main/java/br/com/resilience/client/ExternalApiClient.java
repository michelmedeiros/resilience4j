package br.com.resilience.client;

import br.com.resilience.config.ExternalApiClientConfiguration;
import br.com.resilience.entity.Book;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "externalApi", url = "${gateway.client.external.url}",
        configuration = ExternalApiClientConfiguration.class)
public interface ExternalApiClient {
    @GetMapping("/circuit-breaker")
    @CircuitBreaker(name = "CircuitBreakerService")
    String getCircuitBreaker();

    @GetMapping("/books")
    @CircuitBreaker(name = "CircuitBreakerService")
    List<Book> getAllBooks();

    @GetMapping("/retry")
    String getRetry();

    @GetMapping("/time-limiter")
    String getTimeLimiterApi();

    @GetMapping("/bulkhead")
    String getBulkhead();
    @GetMapping("/rate-limiter")
    String getRateLimiter();
}