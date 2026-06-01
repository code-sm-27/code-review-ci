package com.github.codesm27.codereview.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class SseEmitterService {

    // Map of Github ID -> SseEmitter
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter createEmitter(String githubId) {
        // Keep connection open for 1 hour (3600000L). You can adjust this.
        // A timeout of 0 means infinite, but it's usually better to have a timeout and let client reconnect.
        SseEmitter emitter = new SseEmitter(3600000L); 
        
        emitters.put(githubId, emitter);
        log.info("SSE Connection established for user: {}", githubId);

        emitter.onCompletion(() -> {
            log.info("SSE Connection completed for user: {}", githubId);
            emitters.remove(githubId);
        });

        emitter.onTimeout(() -> {
            log.info("SSE Connection timeout for user: {}", githubId);
            emitter.complete();
            emitters.remove(githubId);
        });

        emitter.onError(e -> {
            log.error("SSE Connection error for user: {}", githubId, e);
            emitters.remove(githubId);
        });

        // Send an initial dummy event to establish connection successfully
        try {
            emitter.send(SseEmitter.event().name("INIT").data("Connected successfully"));
        } catch (IOException e) {
            log.error("Error sending initial SSE event", e);
            emitter.completeWithError(e);
            emitters.remove(githubId);
        }

        return emitter;
    }

    public void sendReviewNotification(String githubId, String repoFullName, Integer prNumber) {
        SseEmitter emitter = emitters.get(githubId);
        if (emitter != null) {
            try {
                String payload = String.format("{\"repoFullName\": \"%s\", \"prNumber\": %d}", repoFullName, prNumber);
                emitter.send(SseEmitter.event().name("review_ready").data(payload));
                log.info("Sent SSE review_ready notification to user {}", githubId);
            } catch (IOException e) {
                log.error("Failed to send SSE notification to user {}", githubId, e);
                emitter.completeWithError(e);
                emitters.remove(githubId);
            }
        } else {
            log.debug("No active SSE connection for user {}. Notification skipped.", githubId);
        }
    }
}
