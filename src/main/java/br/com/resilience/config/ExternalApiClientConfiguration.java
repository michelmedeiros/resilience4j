package br.com.resilience.config;

import br.com.resilience.service.ExternalApiClientFallback;
import feign.Feign;
import io.github.resilience4j.feign.FeignDecorators;
import io.github.resilience4j.feign.Resilience4jFeign;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExternalApiClientConfiguration {
    @Bean
    public Feign.Builder feignExternalBuilder() {
        var decorations = FeignDecorators.builder()
                .withFallbackFactory(ExternalApiClientFallback::new)
                .build();
        return Resilience4jFeign.builder(decorations);
    }
}
