package com.apichangeradar.snapshot.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class SpecFetcher {
    private final WebClient webClient;

    @Retry(name="specFetchRetry")
    @CircuitBreaker(name="specFetchCb", fallbackMethod="fallback")
    public String fetch(String url) {
        return webClient.get()
                .uri(url)
                .retrieve()
                .onStatus(HttpStatusCode::isError, r->r.createException().flatMap(Mono::error))
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(30))
                .block();
    }

    @SuppressWarnings("unused")
    public String fallback(String url, Exception ex) {
        throw new IllegalStateException("Failed to fetch spec from: "+url+" due to: "+ex.getMessage(), ex);
    }
}

