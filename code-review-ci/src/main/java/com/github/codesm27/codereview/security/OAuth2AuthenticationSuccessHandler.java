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
    private final org.springframework.security.oauth2.client.OAuth2AuthorizedClientService authorizedClientService;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken oauthToken = 
                (org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) authentication;
        
        OAuth2User oAuth2User = oauthToken.getPrincipal();
        
        String githubId = String.valueOf(oAuth2User.getAttributes().get("id"));
        String login = (String) oAuth2User.getAttributes().get("login");
        String email = (String) oAuth2User.getAttributes().get("email");
        String avatarUrl = (String) oAuth2User.getAttributes().get("avatar_url");

        // Retrieve the OAuth2 access token
        org.springframework.security.oauth2.client.OAuth2AuthorizedClient client = 
                authorizedClientService.loadAuthorizedClient(
                        oauthToken.getAuthorizedClientRegistrationId(), 
                        oauthToken.getName());
        
        String githubAccessToken = client.getAccessToken().getTokenValue();

        // Save or update user
        User user = userRepository.findByGithubId(githubId).orElse(new User());
        user.setGithubId(githubId);
        user.setUsername(login);
        user.setEmail(email);
        user.setAvatarUrl(avatarUrl);
        user.setGithubAccessToken(githubAccessToken);
        userRepository.save(user);

        // Generate Tokens
        String accessToken = jwtUtil.generateAccessToken(githubId);
        String refreshToken = jwtUtil.generateRefreshToken(githubId);

        // Redirect to frontend with tokens
        String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/oauth2/redirect")
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
