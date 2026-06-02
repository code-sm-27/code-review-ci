package com.github.codesm27.lambda;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class ClaudeClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;

    public ClaudeClient(ObjectMapper objectMapper) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = objectMapper;
        this.apiKey = System.getenv("ANTHROPIC_API_KEY");
    }

    public AiReviewResponse reviewDiff(String diff, String repoFullName, Integer prNumber) throws Exception {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new RuntimeException("ANTHROPIC_API_KEY environment variable is not set");
        }

        String systemPrompt = "You are an expert code reviewer. Review the provided git diff for the pull request. " +
                "You must respond ONLY with a valid JSON object matching this exact schema:\n" +
                "{\n" +
                "  \"comments\": [\n" +
                "    {\n" +
                "      \"file\": \"path/to/file.ext\",\n" +
                "      \"line\": 123,\n" +
                "      \"severity\": \"HIGH|MEDIUM|LOW\",\n" +
                "      \"category\": \"security|bug|performance|style\",\n" +
                "      \"comment\": \"Explanation of the issue and suggested fix\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"summary\": \"Overall summary of the pull request\",\n" +
                "  \"score\": 85\n" +
                "}\n" +
                "Score is out of 100. Provide line numbers that exist in the diff (the right side / added lines). " +
                "Do not include any markdown formatting like ```json in your response, just the raw JSON.";

        String userMessage = String.format("Repository: %s\nPR Number: %d\n\nDiff:\n%s", repoFullName, prNumber, diff);

        Map<String, Object> requestBody = Map.of(
                "model", "claude-sonnet-4-20250514", // As requested by user, noting actual model might be claude-3-5-sonnet-20241022 or similar, but following exact instructions.
                "max_tokens", 1000,
                "system", systemPrompt,
                "messages", new Object[]{
                        Map.of("role", "user", "content", userMessage)
                }
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.anthropic.com/v1/messages"))
                .header("Content-Type", "application/json")
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Claude API failed with status " + response.statusCode() + ": " + response.body());
        }

        JsonNode root = objectMapper.readTree(response.body());
        String textResponse = root.path("content").get(0).path("text").asText();

        // The try-catch for parsing JSON happens in CodeReviewHandler, but we can do it here too
        return objectMapper.readValue(textResponse, AiReviewResponse.class);
    }
}
