package com.github.codesm27.codereview.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GithubWebhookPayload {

    private String action;

    @JsonProperty("pull_request")
    private PullRequest pullRequest;

    @JsonProperty("repository")
    private Repository repository;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PullRequest {
        private Long id;
        private Integer number;
        private String state;
        private String title;

        @JsonProperty("html_url")
        private String htmlUrl;

        @JsonProperty("diff_url")
        private String diffUrl;

        private Head head;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Head {
        private String sha;
        private String ref;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Repository {
        @JsonProperty("full_name")
        private String fullName;
    }

    @JsonProperty("installation")
    private Installation installation;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Installation {
        private String id;
    }
}