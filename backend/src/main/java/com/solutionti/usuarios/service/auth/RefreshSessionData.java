package com.solutionti.usuarios.service.auth;

import java.io.Serializable;
import java.util.UUID;

public record RefreshSessionData(
    UUID userId,
    long createdAtEpochMillis
) implements Serializable {}
