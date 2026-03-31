package com.solutionti.usuarios.service;

import com.solutionti.usuarios.dto.request.LoginRequest;
import com.solutionti.usuarios.dto.response.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);
}
