package com.github.codesm27.codereview.repository;

import com.github.codesm27.codereview.entity.PullRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PullRequestRepository extends JpaRepository<PullRequest, Long> {
    Optional<PullRequest> findByRepositoryIdAndPrNumber(Long repositoryId, Integer prNumber);
    List<PullRequest> findByRepositoryId(Long repositoryId);
}
