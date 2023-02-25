package br.com.resilience.client;

import br.com.resilience.entity.Book;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cloud.client.circuitbreaker.NoFallbackAvailableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.util.List;
import java.util.stream.IntStream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.nio.charset.Charset.defaultCharset;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.util.StreamUtils.copyToString;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ExternalApiClientTest {

    @RegisterExtension
    static WireMockExtension EXTERNAL_SERVICE = WireMockExtension.newInstance()
            .options(WireMockConfiguration.wireMockConfig()
                    .port(9090))
            .build();

    @Autowired
    private ExternalApiClient externalApiClient;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testCircuitBreaker() throws IOException {

        EXTERNAL_SERVICE.stubFor(WireMock.get("/api/v1/external/circuit-breaker")
                .willReturn(WireMock.aResponse().withStatus(200)
                        .withBody("Hello")
                ));


//        EXTERNAL_SERVICE.stubFor(WireMock.get("/api/v1/external/books")
//                .willReturn(WireMock.aResponse()
//                        .withStatus(HttpStatus.OK.value())
//                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
//                        .withBody(copyToString(
//                                        this.getClass().getClassLoader()
//                                                .getResourceAsStream("payload/get-books-response.json"),
//                                        defaultCharset()))));

        IntStream.rangeClosed(1, 5)
                .forEach(i -> {
                    String response = externalApiClient.getCircuitBreaker();
                    assertThat(response).isEqualTo("Hello");
                });

//        IntStream.rangeClosed(1, 5)
//                .forEach(i -> {
////                    String response = externalApiClient.getExternalApi();
////                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
//                });

        EXTERNAL_SERVICE.verify(5, getRequestedFor(urlEqualTo("/api/v1/external/circuit-breaker")));
    }

    @Test
    public void testCircuitBreakerError() throws IOException {

        EXTERNAL_SERVICE.stubFor(WireMock.get("/api/v1/external/circuit-breaker")
                .willReturn(serverError()));

        IntStream.rangeClosed(1, 5)
                .forEach(i -> {
                    NoFallbackAvailableException exception = assertThrows(NoFallbackAvailableException.class, () -> {
                        externalApiClient.getCircuitBreaker();
                    });
                    assertEquals("Boom!", exception.getMessage());
                });
//
        IntStream.rangeClosed(1, 5)
                .forEach(i -> {
                    System.out.println("Request: " + i);
                    assertThrowsExactly(CallNotPermittedException.class, () -> externalApiClient.getCircuitBreaker());
                });

        EXTERNAL_SERVICE.verify(5, getRequestedFor(urlEqualTo("/api/v1/external/circuit-breaker")));
    }

    @Test
    public void testBooks() throws IOException {
        EXTERNAL_SERVICE.stubFor(WireMock.get("/api/v1/external/books")
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(copyToString(
                                        this.getClass().getClassLoader()
                                                .getResourceAsStream("payload/get-books-response.json"),
                                        defaultCharset()))));

        IntStream.rangeClosed(1, 5)
                .forEach(i -> {
                    List<Book> response = externalApiClient.getAllBooks();
                    assertThat(response).isNotEmpty();
                });
        EXTERNAL_SERVICE.verify(5, getRequestedFor(urlEqualTo("/api/v1/external/books")));
    }



}
