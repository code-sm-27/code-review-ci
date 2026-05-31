package com.github.codesm27.codereview.security;

import com.github.codesm27.codereview.entity.User;
import com.github.codesm27.codereview.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        
        String githubId = String.valueOf(oAuth2User.getAttributes().get("id"));
        String login = (String) oAuth2User.getAttributes().get("login");
        String email = (String) oAuth2User.getAttributes().get("email");
        String avatarUrl = (String) oAuth2User.getAttributes().get("avatar_url");

        // Save or update user
        User user = userRepository.findByGithubId(githubId).orElse(new User());
        user.setGithubId(githubId);
        user.setUsername(login);
        user.setEmail(email);
        user.setAvatarUrl(avatarUrl);
        userRepository.save(user);

        // Generate JWT
        String token = jwtUtil.generateToken(githubId);

        // Redirect to frontend with token
        String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/oauth2/redirect")
                .queryParam("token", token)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
