package com.apichangeradar.alert.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomWebhookSender {
    private final WebClient webClient;

    public boolean send(String url, Map<String,Object> payload) {
        try {
            webClient.post().uri(url)
                    .bodyValue(payload)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            return true;
        } catch (Exception e) {
            log.error("Custom webhook failed: {}", e.getMessage());
            return false;
        }
    }
}

