package com.solutionti.usuarios.service.auth;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

public interface RefreshSessionStore {

    String create(UUID userId, Duration ttl);

    Optional<RefreshSessionData> find(String token);

    String rotate(String token, Duration ttl);

    void revoke(String token);
}
