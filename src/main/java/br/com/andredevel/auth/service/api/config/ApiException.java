package br.com.andredevel.auth.service.api.config;

import br.com.andredevel.auth.service.api.model.ErrorResponse;
import lombok.Getter;
import org.apache.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public class ApiException extends RuntimeException {
    private final HttpStatusCode statusCode;
    private final ErrorResponse errorResponse;

    public ApiException(HttpStatusCode statusCode, ErrorResponse errorResponse) {
        super(errorResponse.message());
        this.statusCode = statusCode;
        this.errorResponse = errorResponse;
    }

}

