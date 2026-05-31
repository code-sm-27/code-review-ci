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

    @PostMapping("/repos")
    public ResponseEntity<RepoDto> connectRepo(@RequestBody com.github.codesm27.codereview.dto.request.ConnectRepoRequest request) {
        User user = getCurrentUser();
        
        // Check if repo already connected
        if (repoRepository.findByFullName(request.getFullName()).isPresent()) {
            throw new RuntimeException("Repository already connected");
        }

        com.github.codesm27.codereview.entity.GithubRepo repo = new com.github.codesm27.codereview.entity.GithubRepo();
        repo.setOwner(user);
        repo.setFullName(request.getFullName());
        repo.setDescription(request.getDescription());
        repo.setUrl(request.getUrl());
        
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
    public ResponseEntity<List<ReviewDto>> getReviews(@RequestParam Long prId) {
        // Assume user validation passed for simplicity (could add PR owner check)
        List<ReviewDto> reviews = reviewRepository.findByPullRequestId(prId).stream()
                .map(r -> ReviewDto.builder()
                        .id(r.getId())
                        .prId(r.getPullRequest().getId())
                        .file(r.getFile())
                        .lineNumber(r.getLineNumber())
                        .severity(r.getSeverity().name())
                        .category(r.getCategory())
                        .comment(r.getComment())
                        .createdAt(r.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/analytics/severity")
    public ResponseEntity<Map<String, Long>> getSeverityAnalytics() {
        // Aggregate severity counts for the user's repos (mock logic for now, using all reviews)
        Map<String, Long> severityCounts = reviewRepository.findAll().stream()
                .collect(Collectors.groupingBy(r -> r.getSeverity().name(), Collectors.counting()));
        return ResponseEntity.ok(severityCounts);
    }

    @GetMapping("/analytics/trend")
    public ResponseEntity<Map<String, Long>> getTrendAnalytics() {
        // Group by Date for reviews created
        Map<String, Long> trend = reviewRepository.findAll().stream()
                .collect(Collectors.groupingBy(r -> r.getCreatedAt().toLocalDate().toString(), Collectors.counting()));
        return ResponseEntity.ok(trend);
    }
}
