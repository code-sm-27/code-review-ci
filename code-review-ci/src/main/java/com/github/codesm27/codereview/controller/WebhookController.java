package com.github.codesm27.codereview.controller;

import com.github.codesm27.codereview.dto.GithubWebhookPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/webhooks")
public class WebhookController {

    @PostMapping("/github")
    public ResponseEntity<Void> handleGithubWebhook(
            @RequestHeader(value = "X-GitHub-Event", required = false) String eventType,
            @RequestHeader(value = "X-Hub-Signature-256", required = false) String signature,
            @RequestBody GithubWebhookPayload payload) {

        try {
            // 1. Validate the event type
            if (!"pull_request".equals(eventType)) {
                log.debug("Ignored non-PR event type: {}", eventType);
                return ResponseEntity.ok().build(); // Return 200 to acknowledge receipt of unhandled events
            }

            // 2. Validate essential payload data
            if (payload == null || payload.getPullRequest() == null) {
                log.warn("Received malformed PR payload");
                return ResponseEntity.badRequest().build();
            }

            // 3. Log the ingestion (In Phase 2, this moves to SQS)
            log.info("Received PR Event | Action: {} | Repo: {} | PR #{}: {} | Commit SHA: {}",
                    payload.getAction(),
                    payload.getRepository().getFullName(),
                    payload.getPullRequest().getNumber(),
                    payload.getPullRequest().getTitle(),
                    payload.getPullRequest().getHead().getSha());

            // 4. Return immediately to prevent GitHub timeout
            return ResponseEntity.accepted().build(); // HTTP 202

        } catch (Exception e) {
            log.error("Critical failure during webhook ingestion: {}", e.getMessage(), e);
            // Even on internal errors, we return a standard response to GitHub without exposing internals
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}