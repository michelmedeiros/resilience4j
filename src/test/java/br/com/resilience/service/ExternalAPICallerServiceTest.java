package br.com.resilience.service;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.IntStream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpStatus.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ExternalAPICallerServiceTest {

    @RegisterExtension
    static WireMockExtension EXTERNAL_SERVICE = WireMockExtension.newInstance()
            .options(WireMockConfiguration.wireMockConfig()
                    .port(9090))
            .build();

    @Autowired
    private ExternalAPICallerService externalAPICallerService;


    @Test
    public void testRetry() throws IOException {

        EXTERNAL_SERVICE.stubFor(WireMock.get("/api/v1/external/retry")
                .willReturn(WireMock.aResponse().withStatus(200)
                        .withBody("ok")
                ));
        String response1 = externalAPICallerService.getRetry();
        assertEquals(response1, "ok");
        EXTERNAL_SERVICE.verify(1, getRequestedFor(urlEqualTo("/api/v1/external/retry")));

        EXTERNAL_SERVICE.resetRequests();

        EXTERNAL_SERVICE.stubFor(WireMock.get("/api/v1/external/retry")
                .willReturn(serverError()));
        String response2 = externalAPICallerService.getRetry();
        assertEquals(response2, "all retries have exhausted");
        EXTERNAL_SERVICE.verify(3, getRequestedFor(urlEqualTo("/api/v1/external/retry")));
    }

    @Test
    public void testTimeLimiterAsync() throws ExecutionException, InterruptedException {
        EXTERNAL_SERVICE.stubFor(WireMock.get("/api/v1/async/async-time-limiter")
                .willReturn(WireMock.aResponse().withStatus(200)
                        .withBody("ok")
                ));

        CompletableFuture<String> response1 = externalAPICallerService.callApiWithDelayAsync();
        assertEquals(response1.get(), "ok");

        EXTERNAL_SERVICE.stubFor(WireMock.get("/api/v1/async/async-time-limiter")
                .willReturn(serverError()));
        CompletableFuture<String> response2 = externalAPICallerService.callApiWithDelayAsync();
        assertThrowsExactly(ExecutionException.class, response2::get);
        EXTERNAL_SERVICE.verify(2, getRequestedFor(urlEqualTo("/api/v1/async/async-time-limiter")));
    }

    @Test
    public void testBulkhead() {
        EXTERNAL_SERVICE.stubFor(WireMock.get("/api/v1/external/bulkhead")
                .willReturn(ok()));
        Map<Integer, String> responseStatusCount = new HashMap<>();
        IntStream.rangeClosed(1, 10)
                .parallel()
                .forEach(i -> {
                    try {
                        ResponseEntity<String> result = externalAPICallerService.callBulkhead();
                        responseStatusCount.put(200,  result.getBody());
                    } catch (RuntimeException ex) {
                        responseStatusCount.put(BANDWIDTH_LIMIT_EXCEEDED.value(), "Call" + ex.getMessage() + i);
                    }
                });
        assertTrue(responseStatusCount.containsKey(BANDWIDTH_LIMIT_EXCEEDED.value()));
        assertTrue(responseStatusCount.containsKey(OK.value()));
        EXTERNAL_SERVICE.verify(3,
                getRequestedFor(urlEqualTo("/api/v1/external/bulkhead")));
    }

    @Test
    public void testRateLimiter() {

        EXTERNAL_SERVICE.stubFor(WireMock.get("/api/v1/external/rate-limiter")
                .willReturn(WireMock.aResponse().withStatus(200)
                        .withBody("ok")
                ));
        Map<HttpStatus, String> responseStatusCount = new ConcurrentHashMap<>();
        IntStream.rangeClosed(1, 10)
                .forEach(i -> {
                    if(i <=5) {
                        String response = externalAPICallerService.callRateLimiter();
                        responseStatusCount.put(OK, "ok");
                        assertEquals(response, "ok");
                    } else {
                        RequestNotPermitted exception = assertThrows(RequestNotPermitted.class,
                                () -> externalAPICallerService.callRateLimiter());
                        responseStatusCount.put(TOO_MANY_REQUESTS, exception.getMessage());
                        assertEquals(exception.getMessage(), "RateLimiter 'rateLimiterApi' does not permit further calls");
                    }
                });

        EXTERNAL_SERVICE.verify(5, getRequestedFor(urlEqualTo("/api/v1/external/rate-limiter")));
        assertTrue(responseStatusCount.containsKey(TOO_MANY_REQUESTS));
        assertTrue(responseStatusCount.containsKey(OK));
    }
}
