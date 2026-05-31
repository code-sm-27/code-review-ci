package com.github.codesm27.codereview.repository;

import com.github.codesm27.codereview.entity.GithubRepo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GithubRepoRepository extends JpaRepository<GithubRepo, Long> {
    Optional<GithubRepo> findByFullName(String fullName);
    List<GithubRepo> findByOwnerId(Long ownerId);
}
