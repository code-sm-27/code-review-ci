package com.github.codesm27.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public class CodeReviewHandler implements RequestHandler<SQSEvent, Void> {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final GithubClient githubClient = new GithubClient();
    private final GeminiClient aiClient = new GeminiClient();
    private final InternalApiClient internalApiClient = new InternalApiClient();

    @Override
    public Void handleRequest(SQSEvent event, Context context) {
        for (SQSEvent.SQSMessage message : event.getRecords()) {
            try {
                // Parse SQS Message Payload
                Map<String, Object> payload = objectMapper.readValue(message.getBody(), Map.class);
                String repoFullName = (String) payload.get("repoFullName");
                Integer prNumber = (Integer) payload.get("prNumber");
                Long repoId = ((Number) payload.get("repoId")).longValue();

                context.getLogger().log("Processing review for " + repoFullName + " PR #" + prNumber);

                // 1. Fetch Diff
                String diff = githubClient.fetchPrDiff(repoFullName, prNumber);

                // 2. Call Gemini API
                AiReviewResponse aiResponse = aiClient.analyzeDiff(diff);

                // 3. Post to GitHub (only HIGH and MEDIUM)
                for (AiReviewResponse.Comment c : aiResponse.getComments()) {
                    if ("HIGH".equalsIgnoreCase(c.getSeverity()) || "MEDIUM".equalsIgnoreCase(c.getSeverity())) {
                        String formattedBody = String.format("**[%s] %s**\n%s", 
                                c.getSeverity(), c.getCategory(), c.getComment());
                        
                        // Note: For GitHub PR reviews, you usually need a specific commitId.
                        // Since this is a simplified example, we might fail if commitId is null.
                        // In a real app, we would fetch the PR details to get the HEAD commit SHA.
                        try {
                            githubClient.postReviewComment(repoFullName, prNumber, "HEAD", c.getFile(), c.getLine(), formattedBody);
                        } catch (Exception e) {
                            context.getLogger().log("Failed to post comment for file " + c.getFile() + " at line " + c.getLine());
                        }
                    }
                }

                // 4. Persist to Spring Boot
                internalApiClient.persistReviews(repoId, prNumber, aiResponse);

            } catch (Exception e) {
                context.getLogger().log("Error processing message: " + message.getMessageId() + ". Exception: " + e.getMessage());
                // Throwing the exception ensures the message is returned to SQS and eventually goes to the DLQ (Dead Letter Queue)
                throw new RuntimeException(e);
            }
        }
        return null;
    }
}
