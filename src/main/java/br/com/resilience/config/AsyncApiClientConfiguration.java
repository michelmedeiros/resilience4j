package br.com.resilience.config;

import br.com.resilience.service.AsyncApiClientFallback;
import feign.Feign;
import io.github.resilience4j.feign.FeignDecorators;
import io.github.resilience4j.feign.Resilience4jFeign;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AsyncApiClientConfiguration {
    @Bean
    public Feign.Builder feignAsyncBuilder() {
        var decorations = FeignDecorators.builder()
                .withFallbackFactory(AsyncApiClientFallback::new)
                .build();
        return Resilience4jFeign.builder(decorations);
    }
}
