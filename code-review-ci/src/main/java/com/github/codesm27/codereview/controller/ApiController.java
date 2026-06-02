package com.github.codesm27.codereview.controller;

import com.github.codesm27.codereview.dto.response.RepoDto;
import com.github.codesm27.codereview.dto.response.ReviewDto;
import com.github.codesm27.codereview.entity.User;
import com.github.codesm27.codereview.repository.GithubRepoRepository;
import com.github.codesm27.codereview.repository.ReviewRepository;
import com.github.codesm27.codereview.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ApiController {

    private final GithubRepoRepository repoRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        String githubId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findByGithubId(githubId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping("/repos")
    public ResponseEntity<List<RepoDto>> getRepos() {
        User user = getCurrentUser();
        List<RepoDto> repos = repoRepository.findByOwnerId(user.getId()).stream()
                .map(r -> RepoDto.builder()
                        .id(r.getId())
                        .fullName(r.getFullName())
                        .description(r.getDescription())
                        .url(r.getUrl())
                        .createdAt(r.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(repos);
    }

    @PostMapping("/repos/connect")
    public ResponseEntity<RepoDto> connectRepo(@RequestBody com.github.codesm27.codereview.dto.request.ConnectRepoRequest request) {
        User user = getCurrentUser();
        
        if (repoRepository.findByFullName(request.getFullName()).isPresent()) {
            throw new RuntimeException("Repository already connected");
        }

        String webhookSecret = java.util.UUID.randomUUID().toString(); // Generate unique secret

        com.github.codesm27.codereview.entity.GithubRepo repo = new com.github.codesm27.codereview.entity.GithubRepo();
        repo.setOwner(user);
        repo.setFullName(request.getFullName());
        repo.setDescription(request.getDescription());
        repo.setUrl(request.getUrl());
        repo.setWebhookSecret(webhookSecret);
        
        // TODO: Call GitHub API to register webhook programmatically using user's OAuth token
        // String webhookId = githubClient.registerWebhook(request.getFullName(), webhookSecret);
        
        repo = repoRepository.save(repo);
        
        RepoDto dto = RepoDto.builder()
                .id(repo.getId())
                .fullName(repo.getFullName())
                .description(repo.getDescription())
                .url(repo.getUrl())
                .createdAt(repo.getCreatedAt())
                .build();
                
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/reviews")
    public ResponseEntity<org.springframework.data.domain.Page<ReviewDto>> getReviews(
            @RequestParam Long repoId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        org.springframework.data.domain.Page<ReviewDto> reviews = reviewRepository.findByPullRequestRepositoryId(repoId, pageable)
                .map(r -> ReviewDto.builder()
                        .id(r.getId())
                        .prId(r.getPullRequest().getId())
                        .file(r.getFile())
                        .lineNumber(r.getLineNumber())
                        .severity(r.getSeverity().name())
                        .category(r.getCategory())
                        .comment(r.getComment())
                        .createdAt(r.getCreatedAt())
                        .build());
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/analytics/severity")
    public ResponseEntity<Map<String, Long>> getSeverityAnalytics(@RequestParam Long repoId) {
        Map<String, Long> severityCounts = reviewRepository.findByPullRequestRepositoryId(repoId).stream()
                .collect(Collectors.groupingBy(r -> r.getSeverity().name(), Collectors.counting()));
        return ResponseEntity.ok(severityCounts);
    }

    @GetMapping("/analytics/trend")
    public ResponseEntity<Map<String, Long>> getTrendAnalytics(
            @RequestParam Long repoId,
            @RequestParam(defaultValue = "30") int days) {
        
        java.time.LocalDateTime startDate = java.time.LocalDateTime.now().minusDays(days);
        
        Map<String, Long> trend = reviewRepository.findByPullRequestRepositoryIdAndCreatedAtAfter(repoId, startDate).stream()
                .collect(Collectors.groupingBy(r -> r.getCreatedAt().toLocalDate().toString(), Collectors.counting()));
        return ResponseEntity.ok(trend);
    }
}
