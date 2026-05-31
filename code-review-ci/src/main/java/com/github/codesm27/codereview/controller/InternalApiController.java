package com.github.codesm27.codereview.controller;

import com.github.codesm27.codereview.entity.PullRequest;
import com.github.codesm27.codereview.entity.Review;
import com.github.codesm27.codereview.repository.PullRequestRepository;
import com.github.codesm27.codereview.repository.ReviewRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
@Slf4j
public class InternalApiController {

    private final PullRequestRepository prRepository;
    private final ReviewRepository reviewRepository;

    @PostMapping("/reviews")
    public ResponseEntity<Void> addReviews(@RequestBody ReviewRequest payload) {
        Optional<PullRequest> prOpt = prRepository.findByRepositoryIdAndPrNumber(payload.getRepoId(), payload.getPrNumber());
        
        if (prOpt.isEmpty()) {
            log.error("Received internal review for unknown PR {} on repo {}", payload.getPrNumber(), payload.getRepoId());
            return ResponseEntity.badRequest().build();
        }

        PullRequest pr = prOpt.get();

        for (ReviewData data : payload.getComments()) {
            Review review = new Review();
            review.setPullRequest(pr);
            review.setFile(data.getFile());
            review.setLineNumber(data.getLineNumber());
            review.setSeverity(Review.Severity.valueOf(data.getSeverity().toUpperCase()));
            review.setCategory(data.getCategory());
            review.setComment(data.getComment());
            reviewRepository.save(review);
        }

        return ResponseEntity.ok().build();
    }

    @Data
    public static class ReviewRequest {
        private Long repoId;
        private Integer prNumber;
        private List<ReviewData> comments;
        private String summary;
        private Integer score;
    }

    @Data
    public static class ReviewData {
        private String file;
        private Integer lineNumber;
        private String severity;
        private String category;
        private String comment;
    }
}
