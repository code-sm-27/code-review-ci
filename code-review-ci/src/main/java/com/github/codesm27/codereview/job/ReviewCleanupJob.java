package com.github.codesm27.codereview.job;

import com.github.codesm27.codereview.entity.Review;
import com.github.codesm27.codereview.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class ReviewCleanupJob {

    private final ReviewRepository reviewRepository;

    // Run every day at midnight to delete reviews older than 30 days
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void cleanupOldReviews() {
        LocalDateTime thresholdDate = LocalDateTime.now().minusDays(30);
        log.info("Starting cleanup of reviews older than {}", thresholdDate);

        List<Review> oldReviews = reviewRepository.findByCreatedAtBefore(thresholdDate);
        if (!oldReviews.isEmpty()) {
            reviewRepository.deleteAll(oldReviews);
            log.info("Deleted {} old reviews.", oldReviews.size());
        } else {
            log.info("No old reviews found for cleanup.");
        }
    }
}
