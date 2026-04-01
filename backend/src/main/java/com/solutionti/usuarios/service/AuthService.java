package com.solutionti.usuarios.service;

import com.solutionti.usuarios.dto.request.LoginRequest;
import com.solutionti.usuarios.dto.response.LoginResponse;
import com.solutionti.usuarios.service.auth.AuthSessionResult;

public interface AuthService {

    AuthSessionResult login(LoginRequest request);

    AuthSessionResult refresh(String refreshToken);

    void logout(String refreshToken);

    LoginResponse me();
}
