package br.com.resilience.service;

import br.com.resilience.client.AsyncExternalApiClient;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AsyncApiClientFallback implements AsyncExternalApiClient {

    private final Exception cause;

    public AsyncApiClientFallback(Exception cause) {
        this.cause = cause;
    }

    @Override
    public String getAsyncTimeLimiterApi() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ignore) {
            log.error("Error: {}", ignore.getMessage());
            ignore.printStackTrace();
        }
        return "ok";
    }
}
