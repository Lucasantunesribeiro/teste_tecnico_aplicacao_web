package com.solutionti.usuarios.service.impl;

import com.solutionti.usuarios.dto.request.LoginRequest;
import com.solutionti.usuarios.dto.response.LoginResponse;
import com.solutionti.usuarios.entity.Usuario;
import com.solutionti.usuarios.entity.enums.StatusUsuario;
import com.solutionti.usuarios.exception.UnauthorizedException;
import com.solutionti.usuarios.repository.UsuarioRepository;
import com.solutionti.usuarios.security.JwtTokenProvider;
import com.solutionti.usuarios.security.SecurityUtils;
import com.solutionti.usuarios.service.AuthService;
import com.solutionti.usuarios.service.auth.AuthSessionResult;
import com.solutionti.usuarios.service.auth.RefreshSessionData;
import com.solutionti.usuarios.service.auth.RefreshSessionStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final RefreshSessionStore refreshSessionStore;

    @Value("${jwt.refresh-expiration:604800000}")
    private long refreshTokenExpirationMs;

    @Override
    public AuthSessionResult login(LoginRequest request) {
        log.info("Tentativa de login recebida");

        Usuario usuario = usuarioRepository.findByCpfAndStatus(request.cpf(), StatusUsuario.ATIVO)
            .orElse(null);

        if (usuario == null || !passwordEncoder.matches(request.senha(), usuario.getSenha())) {
            log.warn("Tentativa de login rejeitada");
            throw new UnauthorizedException("Credenciais invalidas");
        }

        AuthSessionResult result = issueSession(usuario);
        log.info("Login realizado com sucesso para usuario ID: {}", usuario.getId());
        return result;
    }

    @Override
    public AuthSessionResult refresh(String refreshToken) {
        log.info("Tentativa de refresh de sessao recebida");

        RefreshSessionData sessionData = refreshSessionStore.find(refreshToken)
            .orElseThrow(() -> {
                log.warn("Refresh rejeitado: token ausente, invalido ou revogado");
                return new UnauthorizedException("Refresh token invalido");
            });

        Usuario usuario = usuarioRepository.findById(sessionData.userId())
            .filter(loaded -> loaded.getStatus() == StatusUsuario.ATIVO)
            .orElseThrow(() -> {
                log.warn("Refresh rejeitado: sessao vinculada a usuario invalido ou inativo");
                return new UnauthorizedException("Sessao invalida");
            });

        String rotatedRefreshToken = refreshSessionStore.rotate(refreshToken, Duration.ofMillis(refreshTokenExpirationMs));
        String accessToken = jwtTokenProvider.generateAccessToken(usuario);

        log.info("Refresh realizado com sucesso para usuario ID: {}", usuario.getId());

        return new AuthSessionResult(
            accessToken,
            rotatedRefreshToken,
            toResponse(usuario)
        );
    }

    @Override
    public void logout(String refreshToken) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            refreshSessionStore.revoke(refreshToken);
            log.info("Logout processado com revogacao de refresh token");
            return;
        }

        log.info("Logout processado sem refresh token associado");
    }

    @Override
    public LoginResponse me() {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            log.warn("Consulta de sessao atual rejeitada: usuario nao autenticado");
            throw new UnauthorizedException("Nao autenticado");
        }

        Usuario usuario = usuarioRepository.findById(currentUserId)
            .filter(loaded -> loaded.getStatus() == StatusUsuario.ATIVO)
            .orElseThrow(() -> {
                log.warn("Consulta de sessao atual rejeitada: usuario invalido ou inativo");
                return new UnauthorizedException("Sessao invalida");
            });

        return toResponse(usuario);
    }

    private AuthSessionResult issueSession(Usuario usuario) {
        String accessToken = jwtTokenProvider.generateAccessToken(usuario);
        String refreshToken = refreshSessionStore.create(usuario.getId(), Duration.ofMillis(refreshTokenExpirationMs));

        return new AuthSessionResult(
            accessToken,
            refreshToken,
            toResponse(usuario)
        );
    }

    private LoginResponse toResponse(Usuario usuario) {
        return new LoginResponse(
            jwtTokenProvider.getAccessTokenExpirationSeconds(),
            usuario.getId(),
            usuario.getNome(),
            usuario.getCpf(),
            usuario.getTipo().name()
        );
    }
}
