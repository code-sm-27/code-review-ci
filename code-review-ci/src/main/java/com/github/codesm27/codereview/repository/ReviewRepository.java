package com.github.codesm27.codereview.repository;

import com.github.codesm27.codereview.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByPullRequestId(Long pullRequestId);
    List<Review> findByCreatedAtBefore(LocalDateTime threshold);
    
    org.springframework.data.domain.Page<Review> findByPullRequestRepositoryId(Long repoId, org.springframework.data.domain.Pageable pageable);
    
    List<Review> findByPullRequestRepositoryId(Long repoId);
    
    List<Review> findByPullRequestRepositoryIdAndCreatedAtAfter(Long repoId, LocalDateTime createdAt);
}
