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
    private final String githubToken;

    public GithubClient() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        // Fallback for demo, in production we should retrieve token from DB or secure vault
        this.githubToken = System.getenv("GITHUB_TOKEN");
    }

    public String fetchPrDiff(String repoFullName, Integer prNumber) throws Exception {
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

    public void postReviewComment(String repoFullName, Integer prNumber, String commitId, String path, int line, String body) throws Exception {
        String url = String.format("https://api.github.com/repos/%s/pulls/%s/comments", repoFullName, prNumber);
        
        Map<String, Object> payload = Map.of(
                "body", body,
                "commit_id", commitId,
                "path", path,
                "line", line
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
        
        if (response.statusCode() >= 400) {
            System.err.println("Failed to post comment to GitHub. Status: " + response.statusCode() + ", Body: " + response.body());
        }
    }
}
