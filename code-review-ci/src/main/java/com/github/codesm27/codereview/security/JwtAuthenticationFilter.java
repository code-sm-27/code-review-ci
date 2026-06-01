package com.github.codesm27.codereview.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        String jwt = null;
        String githubId;

        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);
        } else {
            // Check for token in query parameters (for SSE and WebSockets)
            String tokenParam = request.getParameter("token");
            if (StringUtils.hasText(tokenParam)) {
                jwt = tokenParam;
            }
        }

        if (!StringUtils.hasText(jwt)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        try {
            githubId = jwtUtil.extractGithubId(jwt);
            
            if (githubId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                if (jwtUtil.isTokenValid(jwt, githubId)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            githubId,
                            null,
                            Collections.emptyList()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Invalid token, do not set authentication
        }

        filterChain.doFilter(request, response);
    }
}
