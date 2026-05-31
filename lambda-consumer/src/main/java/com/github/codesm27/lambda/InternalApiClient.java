package com.github.codesm27.lambda;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InternalApiClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String backendUrl;
    private final String internalApiKey;

    public InternalApiClient() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.backendUrl = System.getenv("BACKEND_URL"); // e.g., http://backend:8080/internal/reviews
        this.internalApiKey = System.getenv("INTERNAL_API_KEY");
    }

    public void persistReviews(Long repoId, Integer prNumber, AiReviewResponse aiResponse) throws Exception {
        // Filter to only persist HIGH and MEDIUM severity
        List<Map<String, Object>> filteredComments = aiResponse.getComments().stream()
                .filter(c -> "HIGH".equalsIgnoreCase(c.getSeverity()) || "MEDIUM".equalsIgnoreCase(c.getSeverity()))
                .map(c -> {
                    Map<String, Object> map = new java.util.HashMap<>();
                    map.put("file", c.getFile());
                    map.put("lineNumber", c.getLine());
                    map.put("severity", c.getSeverity());
                    map.put("category", c.getCategory());
                    map.put("comment", c.getComment());
                    return map;
                })
                .collect(Collectors.toList());

        Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("repoId", repoId);
        payload.put("prNumber", prNumber);
        payload.put("summary", aiResponse.getSummary());
        payload.put("score", aiResponse.getScore());
        payload.put("comments", filteredComments);

        String jsonPayload = objectMapper.writeValueAsString(payload);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(backendUrl))
                .header("Content-Type", "application/json")
                .header("X-Internal-Api-Key", internalApiKey != null ? internalApiKey : "mock_internal_key")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 400) {
            System.err.println("Failed to persist reviews to Backend. Status: " + response.statusCode());
        }
    }
}
