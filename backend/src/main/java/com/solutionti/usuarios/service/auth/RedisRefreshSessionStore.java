package com.solutionti.usuarios.service.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisRefreshSessionStore implements RefreshSessionStore {

    private static final String KEY_PREFIX = "auth:refresh:";

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public String create(UUID userId, Duration ttl) {
        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(
            buildKey(token),
            new RefreshSessionData(userId, System.currentTimeMillis()),
            ttl
        );
        return token;
    }

    @Override
    public Optional<RefreshSessionData> find(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }

        Object data = redisTemplate.opsForValue().get(buildKey(token));
        if (data instanceof RefreshSessionData refreshSessionData) {
            return Optional.of(refreshSessionData);
        }

        return Optional.empty();
    }

    @Override
    public String rotate(String token, Duration ttl) {
        RefreshSessionData data = find(token)
            .orElseThrow(() -> new IllegalStateException("Refresh token nao encontrado para rotacao"));

        revoke(token);
        return create(data.userId(), ttl);
    }

    @Override
    public void revoke(String token) {
        if (token == null || token.isBlank()) {
            return;
        }

        redisTemplate.delete(buildKey(token));
    }

    private String buildKey(String token) {
        return KEY_PREFIX + token;
    }
}
