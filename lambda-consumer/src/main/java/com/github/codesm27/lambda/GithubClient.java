package com.github.codesm27.lambda;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

public class GithubClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    public GithubClient() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public String fetchPrDiff(String repoFullName, Integer prNumber, String githubToken) throws Exception {
        String url = String.format("https://api.github.com/repos/%s/pulls/%s", repoFullName, prNumber);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + githubToken)
                .header("Accept", "application/vnd.github.v3.diff")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to fetch diff from GitHub. Status: " + response.statusCode());
        }

        return response.body();
    }

    public void postReview(String repoFullName, Integer prNumber, String commitId, List<Map<String, Object>> comments, String githubToken) throws Exception {
        if (comments == null || comments.isEmpty()) {
            return;
        }

        String url = String.format("https://api.github.com/repos/%s/pulls/%s/reviews", repoFullName, prNumber);
        
        Map<String, Object> payload = Map.of(
                "commit_id", commitId,
                "event", "COMMENT",
                "comments", comments
        );
        
        String jsonPayload = objectMapper.writeValueAsString(payload);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + githubToken)
                .header("Accept", "application/vnd.github.v3+json")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 422) {
            System.err.println("GitHub rejected review (422) - possibly unchanged lines or invalid commit id: " + response.body());
        } else if (response.statusCode() >= 400) {
            String errorMsg = "Failed to post review to GitHub. Status: " + response.statusCode() + ", Body: " + response.body();
            System.err.println(errorMsg);
            throw new RuntimeException(errorMsg);
        }
    }
}
