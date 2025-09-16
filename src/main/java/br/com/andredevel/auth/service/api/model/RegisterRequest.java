package br.com.andredevel.auth.service.api.model;

public record RegisterRequest(String name, String email, String password) {
}
