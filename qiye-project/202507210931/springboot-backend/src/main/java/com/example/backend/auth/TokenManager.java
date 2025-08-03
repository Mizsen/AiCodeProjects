package com.example.backend.auth;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TokenManager {
    private final Map<String, String> tokenUserMap = new ConcurrentHashMap<>();

    public String generateToken(String username) {
        String token = UUID.randomUUID().toString().replaceAll("-", "");
        tokenUserMap.put(token, username);
        return token;
    }

    public String getUsername(String token) {
        return tokenUserMap.get(token);
    }

    public void removeToken(String token) {
        tokenUserMap.remove(token);
    }
}
