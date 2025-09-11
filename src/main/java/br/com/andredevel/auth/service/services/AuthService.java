package br.com.andredevel.auth.service.services;

import br.com.andredevel.auth.service.api.model.AuthRequest;
import br.com.andredevel.auth.service.api.model.AuthResponse;
import br.com.andredevel.auth.service.api.model.UserVO;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class AuthService {

    private final WebClient webClient;
    private final JwtUtil jwtUtil;

    public AuthService(WebClient.Builder webClientBuilder, JwtUtil jwtUtil) {
        this.webClient = webClientBuilder.build();
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse login(AuthRequest authRequest) {
        String url = "http://user-service/users/login";

        UserVO userLogged = webClient.post()
                .uri(url)
                .bodyValue(authRequest)
                .retrieve()
                .bodyToMono(UserVO.class)
                .block(); // Para converter para síncrono

        // Gera os tokens JWT
        String accessToken = jwtUtil.generateToken(userLogged.id().toString(), "USER", "ACCESS");
        String refreshToken = jwtUtil.generateToken(userLogged.id().toString(), "USER", "REFRESH");

        return new AuthResponse(accessToken, refreshToken);
    }
}