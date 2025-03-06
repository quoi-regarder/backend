package fr.quoi_regarder.security.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Service
public class TokenServiceSimple {
    private final Map<String, String> activeTokens = new ConcurrentHashMap<>();

    public void saveToken(String username, String token) {
        activeTokens.put(username, token);
    }

    public boolean isTokenValid(String username, String token) {
        String activeToken = activeTokens.get(username);
        return activeToken != null && activeToken.equals(token);
    }

    public void invalidateToken(String username) {
        activeTokens.remove(username);
    }
}