package com.solutionti.usuarios.security;

import com.solutionti.usuarios.exception.TooManyRequestsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class LoginRateLimiter {

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration WINDOW = Duration.ofMinutes(15);
    private static final String KEY_PREFIX = "login:attempts:";

    // Lua script: INCR + EXPIRE são executados atomicamente no mesmo round-trip.
    // Garante que a chave sempre terá TTL mesmo se o processo morrer após o INCR.
    private static final DefaultRedisScript<Long> INCR_WITH_EXPIRE_SCRIPT =
        new DefaultRedisScript<>(
            "local n = redis.call('INCR', KEYS[1])\n" +
            "if n == 1 then redis.call('EXPIRE', KEYS[1], ARGV[1]) end\n" +
            "return n",
            Long.class
        );

    private final StringRedisTemplate redisTemplate;

    /**
     * Verifica se o identificador (IP do cliente) excedeu o limite de tentativas.
     * Lança TooManyRequestsException se o limite for ultrapassado.
     */
    public void checkRateLimit(String identifier) {
        String key = KEY_PREFIX + identifier;

        Long attempts = redisTemplate.execute(
            INCR_WITH_EXPIRE_SCRIPT,
            List.of(key),
            String.valueOf(WINDOW.getSeconds())
        );

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
