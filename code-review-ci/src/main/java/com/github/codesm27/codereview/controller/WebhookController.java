package com.github.codesm27.codereview.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.codesm27.codereview.dto.GithubWebhookPayload;
import com.github.codesm27.codereview.entity.GithubRepo;
import com.github.codesm27.codereview.entity.PullRequest;
import com.github.codesm27.codereview.repository.GithubRepoRepository;
import com.github.codesm27.codereview.repository.PullRequestRepository;
import com.github.codesm27.codereview.service.HmacSignatureValidator;
import com.github.codesm27.codereview.service.SqsProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
public class WebhookController {

    private final HmacSignatureValidator signatureValidator;
    private final SqsProducerService sqsProducerService;
    private final ObjectMapper objectMapper;
    private final GithubRepoRepository repoRepository;
    private final PullRequestRepository prRepository;

    @PostMapping("/github")
    public ResponseEntity<Void> handleGithubWebhook(
            @RequestHeader(value = "X-GitHub-Event", required = false) String eventType,
            @RequestHeader(value = "X-Hub-Signature-256", required = false) String signature,
            @RequestBody String rawPayload) {

        try {
            // 1. Validate the event type
            if (!"pull_request".equals(eventType)) {
                log.debug("Ignored non-PR event type: {}", eventType);
                return ResponseEntity.ok().build();
            }

            // 2. Validate HMAC-SHA256 signature
            if (!signatureValidator.isValidSignature(rawPayload, signature)) {
                log.warn("Invalid GitHub webhook signature");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // 3. Parse Payload
            GithubWebhookPayload payload = objectMapper.readValue(rawPayload, GithubWebhookPayload.class);

            if (payload == null || payload.getPullRequest() == null || payload.getRepository() == null) {
                log.warn("Received malformed PR payload");
                return ResponseEntity.badRequest().build();
            }

            // Only process opened or synchronize actions
            String action = payload.getAction();
            if (!"opened".equals(action) && !"synchronize".equals(action)) {
                log.debug("Ignored PR action: {}", action);
                return ResponseEntity.ok().build();
            }

            String repoFullName = payload.getRepository().getFullName();
            Integer prNumber = payload.getPullRequest().getNumber();
            String diffUrl = payload.getPullRequest().getDiffUrl(); // Need to ensure diffUrl is added to DTO

            log.info("Received valid PR Event | Action: {} | Repo: {} | PR #{}: {}",
                    action, repoFullName, prNumber, payload.getPullRequest().getTitle());

            // Check if repo is registered in our DB
            Optional<GithubRepo> repoOpt = repoRepository.findByFullName(repoFullName);
            if (repoOpt.isEmpty()) {
                log.warn("Repository {} is not registered in CodeReview CI. Ignoring event.", repoFullName);
                return ResponseEntity.ok().build();
            }

            GithubRepo repo = repoOpt.get();

            // Save/Update PullRequest record
            PullRequest pr = prRepository.findByRepositoryIdAndPrNumber(repo.getId(), prNumber)
                    .orElse(new PullRequest());
            pr.setRepository(repo);
            pr.setPrNumber(prNumber);
            pr.setTitle(payload.getPullRequest().getTitle());
            pr.setStatus(payload.getPullRequest().getState());
            pr.setDiffUrl(diffUrl);
            prRepository.save(pr);

            // 4. Enqueue to SQS for background Lambda processing
            sqsProducerService.enqueuePullRequest(repoFullName, prNumber, diffUrl, repo.getId());

            // 5. Return immediately to prevent GitHub timeout
            return ResponseEntity.accepted().build(); // HTTP 202

        } catch (Exception e) {
            log.error("Critical failure during webhook ingestion: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}