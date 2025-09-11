package br.com.andredevel.auth.service.api.model;

import java.util.UUID;

public record UserVO(UUID id, String name, String email) {
}
