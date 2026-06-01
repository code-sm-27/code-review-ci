package com.github.codesm27.codereview.controller;

import com.github.codesm27.codereview.entity.User;
import com.github.codesm27.codereview.repository.UserRepository;
import com.github.codesm27.codereview.service.SseEmitterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/sse")
@RequiredArgsConstructor
public class SseController {

    private final SseEmitterService sseEmitterService;
    private final UserRepository userRepository;

    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe() {
        String githubId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findByGithubId(githubId)
                .orElseThrow(() -> new RuntimeException("User not found"));
                
        return sseEmitterService.createEmitter(user.getGithubId());
    }
}
