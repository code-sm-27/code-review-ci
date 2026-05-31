package com.github.codesm27.codereview.service;

import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class SqsProducerService {

    private final SqsTemplate sqsTemplate;

    @Value("${app.sqs.queue-name}")
    private String queueName;

    public void enqueuePullRequest(String repoFullName, Integer prNumber, String diffUrl, Long repoId) {
        Map<String, Object> payload = Map.of(
                "repoFullName", repoFullName,
                "prNumber", prNumber,
                "diffUrl", diffUrl,
                "repoId", repoId
        );

        sqsTemplate.send(queueName, payload);
        log.info("Enqueued PR {} for repo {} to SQS queue {}", prNumber, repoFullName, queueName);
    }
}
