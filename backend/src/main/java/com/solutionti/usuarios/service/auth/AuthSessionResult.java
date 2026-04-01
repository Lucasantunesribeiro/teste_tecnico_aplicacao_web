package com.solutionti.usuarios.service.auth;

import com.solutionti.usuarios.dto.response.LoginResponse;

public record AuthSessionResult(
    String accessToken,
    String refreshToken,
    LoginResponse response
) {}
