package br.com.resilience.client;

import br.com.resilience.config.AsyncApiClientConfiguration;
import br.com.resilience.config.ExternalApiClientConfiguration;
import br.com.resilience.entity.Book;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "asyncTimeLimiterApi", url = "${gateway.client.async.url}",
configuration = AsyncApiClientConfiguration.class)
public interface AsyncExternalApiClient {
    @GetMapping("/async-time-limiter")
    String getAsyncTimeLimiterApi();
}