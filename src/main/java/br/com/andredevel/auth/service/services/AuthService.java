package br.com.andredevel.auth.service.services;

import br.com.andredevel.auth.service.api.config.ApiException;
import br.com.andredevel.auth.service.api.model.AuthRequest;
import br.com.andredevel.auth.service.api.model.AuthResponse;
import br.com.andredevel.auth.service.api.model.ErrorResponse;
import br.com.andredevel.auth.service.api.model.RegisterRequest;
import br.com.andredevel.auth.service.api.model.UserVO;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class AuthService {

    private static final String SERVICE_USERS_REGISTER = "http://user-service/users/register";
    private static final String SERVICE_USERS_LOGIN = "http://user-service/users/login";
    
    private final WebClient webClient;
    private final JwtUtils jwtUtil;

    public AuthService(WebClient.Builder webClientBuilder, JwtUtils jwtUtil) {
        this.webClient = webClientBuilder.build();
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse login(AuthRequest authRequest) {
        UserVO userLogged = getUserByAuthRequestAndUri(authRequest, SERVICE_USERS_LOGIN);
        return generateTokenByUser(userLogged);
    }

    public AuthResponse register(RegisterRequest request) {
        UserVO userLogged = getUserByAuthRequestAndUri(request, SERVICE_USERS_REGISTER);
        return generateTokenByUser(userLogged);
    }

    private AuthResponse generateTokenByUser(UserVO userLogged) {
        String accessToken = jwtUtil.generateToken(userLogged.id().toString(), "USER", "ACCESS");
        String refreshToken = jwtUtil.generateToken(userLogged.id().toString(), "USER", "REFRESH");

        return new AuthResponse(accessToken, refreshToken);
    }

    private UserVO getUserByAuthRequestAndUri(Object request, String uri) {
        return webClient.post()
                .uri(uri)
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(ErrorResponse.class)
                                .flatMap(error -> Mono.error(new ApiException(response.statusCode(), error)))
                )
                .bodyToMono(UserVO.class)
                .block();
    }
}