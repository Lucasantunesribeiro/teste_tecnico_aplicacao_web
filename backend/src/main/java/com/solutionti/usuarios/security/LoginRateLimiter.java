package com.solutionti.usuarios.security;

import com.solutionti.usuarios.exception.TooManyRequestsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class LoginRateLimiter {

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration WINDOW = Duration.ofMinutes(15);
    private static final String KEY_PREFIX = "login:attempts:";

    private final StringRedisTemplate redisTemplate;

    /**
     * Verifica se o identificador (IP do cliente) excedeu o limite de tentativas.
     * Lança TooManyRequestsException se o limite for ultrapassado.
     */
    public void checkRateLimit(String identifier) {
        String key = KEY_PREFIX + identifier;
        Long attempts = redisTemplate.opsForValue().increment(key);

        if (attempts != null && attempts == 1) {
            redisTemplate.expire(key, WINDOW);
        }

        if (attempts != null && attempts > MAX_ATTEMPTS) {
            Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            long waitSeconds = (ttl != null && ttl > 0) ? ttl : WINDOW.getSeconds();
            log.warn("Rate limit excedido para {}: {} tentativas consecutivas, bloqueado por {}s",
                identifier, attempts, waitSeconds);
            throw new TooManyRequestsException(
                String.format("Muitas tentativas de login. Tente novamente em %d segundos.", waitSeconds)
            );
        }
    }

    /**
     * Reseta o contador após login bem-sucedido.
     */
    public void resetAttempts(String identifier) {
        redisTemplate.delete(KEY_PREFIX + identifier);
    }
}
