package com.github.codesm27.lambda;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

public class GeminiClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String geminiApiKey;

    private static final String SYSTEM_PROMPT = "You are a senior software engineer. Analyze this code diff and return ONLY valid JSON — no markdown, no preamble. Schema: {\"comments\":[{\"file\":\"string\",\"line\":123,\"severity\":\"HIGH|MEDIUM|LOW\",\"category\":\"security|performance|bug|style\",\"comment\":\"string\"}],\"summary\":\"string\",\"score\":100}. Severity: HIGH|MEDIUM|LOW. Category: security|performance|bug|style";

    public GeminiClient() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.geminiApiKey = System.getenv("GEMINI_API_KEY");
    }

    public AiReviewResponse analyzeDiff(String diff) throws Exception {
        // Truncate diff to ~6000 tokens (approx 24000 characters) to be safe, though Gemini 1.5 Pro has a large context window
        if (diff != null && diff.length() > 24000) {
            diff = diff.substring(0, 24000);
        }

        Map<String, Object> payload = Map.of(
            "systemInstruction", Map.of(
                "parts", List.of(Map.of("text", SYSTEM_PROMPT))
            ),
            "contents", List.of(
                Map.of("parts", List.of(Map.of("text", "Please review this code diff:\n\n" + diff)))
            )
        );

        String jsonPayload = objectMapper.writeValueAsString(payload);
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-pro:generateContent?key=" + geminiApiKey;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to call Gemini API. Status: " + response.statusCode() + ", Body: " + response.body());
        }

        // Parse Gemini's response structure
        Map<String, Object> responseMap = objectMapper.readValue(response.body(), Map.class);
        List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseMap.get("candidates");
        Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
        List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
        String textResponse = (String) parts.get(0).get("text");

        // Clean up markdown block if Gemini included it (e.g., ```json\n...\n```)
        if (textResponse.startsWith("```json")) {
            textResponse = textResponse.substring(7);
        } else if (textResponse.startsWith("```")) {
            textResponse = textResponse.substring(3);
        }
        if (textResponse.endsWith("```")) {
            textResponse = textResponse.substring(0, textResponse.length() - 3);
        }
        
        textResponse = textResponse.trim();

        // The textResponse should be pure JSON based on our prompt
        return objectMapper.readValue(textResponse, AiReviewResponse.class);
    }
}
