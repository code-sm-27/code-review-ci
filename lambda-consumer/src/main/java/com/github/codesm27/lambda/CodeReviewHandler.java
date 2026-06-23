package com.github.codesm27.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public class CodeReviewHandler implements RequestHandler<SQSEvent, Void> {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final GithubClient githubClient = new GithubClient();
    private final ClaudeClient aiClient = new ClaudeClient(objectMapper);
    private final InternalApiClient internalApiClient = new InternalApiClient();

    @Override
    public Void handleRequest(SQSEvent event, Context context) {
        for (SQSEvent.SQSMessage message : event.getRecords()) {
            try {
                // Parse SQS Message Payload
                Map<String, Object> payload = objectMapper.readValue(message.getBody(), new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
                String repoFullName = (String) payload.get("repoFullName");
                Integer prNumber = (Integer) payload.get("prNumber");
                Long repoId = ((Number) payload.get("repoId")).longValue();

                context.getLogger().log("Processing review for " + repoFullName + " PR #" + prNumber);

                // 1. Fetch User's GitHub Token from Spring Boot
                String githubToken = internalApiClient.getGithubToken(repoId);

                // 2. Fetch Diff
                String diff = githubClient.fetchPrDiff(repoFullName, prNumber, githubToken);
                if (diff != null && diff.length() > 6000) {
                    diff = diff.substring(0, 6000); // Truncate oversized diffs
                }

                // 3. Call Claude API with retry logic
                AiReviewResponse aiResponse = null;
                int maxRetries = 3;
                int attempts = 0;
                
                while (attempts < maxRetries) {
                    try {
                        attempts++;
                        context.getLogger().log("Calling Claude API, attempt " + attempts + " of " + maxRetries);
                        aiResponse = aiClient.reviewDiff(diff, repoFullName, prNumber);
                        break; // Success
                    } catch (Exception e) {
                        context.getLogger().log("JSON Parsing or Claude API error on attempt " + attempts + ". Exception: " + e.getMessage());
                        if (attempts >= maxRetries) {
                            context.getLogger().log("Max retries reached. Skipping posting for PR #" + prNumber);
                        } else {
                            try {
                                Thread.sleep(2000L * attempts); // Exponential backoff (2s, 4s)
                            } catch (InterruptedException ie) {
                                Thread.currentThread().interrupt();
                                break;
                            }
                        }
                    }
                }

                if (aiResponse == null) {
                    continue; // Skip processing if all retries failed
                }

                // 4. Post to GitHub (only HIGH and MEDIUM)
                java.util.List<Map<String, Object>> githubComments = new java.util.ArrayList<>();
                for (AiReviewResponse.Comment c : aiResponse.getComments()) {
                    if ("HIGH".equalsIgnoreCase(c.getSeverity()) || "MEDIUM".equalsIgnoreCase(c.getSeverity())) {
                        String formattedBody = String.format("**[%s] %s**\n%s", 
                                c.getSeverity(), c.getCategory(), c.getComment());
                        
                        githubComments.add(Map.of(
                                "path", c.getFile(),
                                "line", c.getLine(),
                                "body", formattedBody
                        ));
                    }
                }
                
                // Note: Real commitId is needed for GitHub PR reviews. Using HEAD as placeholder.
                // If it fails with 422, it will be caught and logged by GithubClient.
                if (!githubComments.isEmpty()) {
                    githubClient.postReview(repoFullName, prNumber, "HEAD", githubComments, githubToken);
                }

                // 5. Persist to Spring Boot
                internalApiClient.persistReviews(repoId, prNumber, aiResponse);

            } catch (Exception e) {
                context.getLogger().log("Error processing message: " + message.getMessageId() + ". Exception: " + e.getMessage());
                // Throwing the exception ensures the message is returned to SQS and eventually goes to the DLQ
                throw new RuntimeException(e);
            }
        }
        return null;
    }
}
