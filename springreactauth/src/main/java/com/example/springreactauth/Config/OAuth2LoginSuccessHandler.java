package com.example.springreactauth.Config;


import com.example.springreactauth.Entity.User;
import com.example.springreactauth.Repository.UserRepository;
import com.example.springreactauth.Util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        OAuth2User oauthUser = token.getPrincipal();

        String email = oauthUser.getAttribute("email");
        String username = ((String) oauthUser.getAttribute("name")).replaceAll("\\s+", "");


        // 1. Check if user exists, if not, register them
        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;
        if (userOptional.isEmpty()) {
            user = new User(username, email, "{noop}oauth-password"); // Dummy password
            userRepository.save(user);
        } else {
            user = userOptional.get();
        }

        // 2. Generate your application's JWT
        String jwt = jwtUtils.generateTokenFromUsername(user.getUsername()); // Requires a new method in JwtUtils

        // 3. Redirect to frontend with JWT (e.g., /login?token=...)
        String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:5173/oauth2/redirect")
                .queryParam("token", jwt)
                .queryParam("id", user.getId())
                .queryParam("username", user.getUsername())
                .queryParam("email", user.getEmail())
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
