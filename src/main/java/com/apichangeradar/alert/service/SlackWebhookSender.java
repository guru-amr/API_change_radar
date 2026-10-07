package com.apichangeradar.alert.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class SlackWebhookSender {
    private final WebClient webClient;

    public boolean send(String webhookUrl, String text) {
        try {
            webClient.post().uri(webhookUrl)
                    .bodyValue(Map.of("text", text))
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            return true;
        } catch (Exception e) {
            log.error("Slack webhook failed: {}", e.getMessage());
            return false;
        }
    }
}

