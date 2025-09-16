package br.com.andredevel.auth.service.services;

import br.com.andredevel.auth.service.api.model.AuthRequest;
import br.com.andredevel.auth.service.api.model.AuthResponse;
import br.com.andredevel.auth.service.api.model.RegisterRequest;
import br.com.andredevel.auth.service.api.model.UserVO;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class AuthService {

    private final WebClient webClient;
    private final JwtUtils jwtUtil;

    public AuthService(WebClient.Builder webClientBuilder, JwtUtils jwtUtil) {
        this.webClient = webClientBuilder.build();
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse login(AuthRequest authRequest) {
        String url = "http://user-service/users/login";

        try {
            UserVO userLogged = webClient.post()
                    .uri(url)
                    .bodyValue(authRequest)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError,
                            response -> response.bodyToMono(String.class)
                                    .map(body -> new IllegalArgumentException("Invalid credentials")))
                    .onStatus(HttpStatusCode::is5xxServerError,
                            response -> response.bodyToMono(String.class)
                                    .map(body -> new IllegalStateException("Server error")))    
                    .bodyToMono(UserVO.class)
                    .block();

            String accessToken = jwtUtil.generateToken(userLogged.id().toString(), "USER", "ACCESS");
            String refreshToken = jwtUtil.generateToken(userLogged.id().toString(), "USER", "REFRESH");

            return new AuthResponse(accessToken, refreshToken);
        } catch (Exception e) {
            throw new RuntimeException("Authentication failed: " + e.getMessage(), e);
        }
    }

    public AuthResponse register(RegisterRequest request) {
        String url = "http://user-service/users/register";

        try {
            UserVO userRegistered = webClient.post()
                    .uri(url)
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError,
                            response -> response.bodyToMono(String.class)
                                    .map(body -> new IllegalArgumentException("Registration failed: " + body)))
                    .onStatus(HttpStatusCode::is5xxServerError,
                            response -> response.bodyToMono(String.class)
                                    .map(body -> new IllegalStateException("Server error")))
                    .bodyToMono(UserVO.class)
                    .block();

            String accessToken = jwtUtil.generateToken(userRegistered.id().toString(), "USER", "ACCESS");
            String refreshToken = jwtUtil.generateToken(userRegistered.id().toString(), "USER", "REFRESH");

            return new AuthResponse(accessToken, refreshToken);
        } catch (Exception e) {
            throw new RuntimeException("Registration failed: " + e.getMessage(), e);
        }
    }
}