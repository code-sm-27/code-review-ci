package com.github.codesm27.codereview.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pr_id", nullable = false)
    private PullRequest pullRequest;

    @Column(columnDefinition = "TEXT")
    private String file;

    private Integer lineNumber;

    @Enumerated(EnumType.STRING)
    private Severity severity; // HIGH, MEDIUM, LOW

    private String category; // security, performance, bug, style

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public enum Severity {
        HIGH, MEDIUM, LOW
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
