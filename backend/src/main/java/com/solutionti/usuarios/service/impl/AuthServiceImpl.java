package com.solutionti.usuarios.service.impl;

import com.solutionti.usuarios.dto.request.LoginRequest;
import com.solutionti.usuarios.dto.response.LoginResponse;
import com.solutionti.usuarios.entity.Usuario;
import com.solutionti.usuarios.entity.enums.StatusUsuario;
import com.solutionti.usuarios.exception.BusinessException;
import com.solutionti.usuarios.exception.NotFoundException;
import com.solutionti.usuarios.repository.UsuarioRepository;
import com.solutionti.usuarios.security.JwtTokenProvider;
import com.solutionti.usuarios.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Override
    public LoginResponse login(LoginRequest request) {
        log.info("Tentativa de login recebida");

        Usuario usuario = usuarioRepository.findByCpf(request.cpf())
            .orElseThrow(() -> new NotFoundException("Credenciais inválidas"));

        if (!StatusUsuario.ATIVO.equals(usuario.getStatus())) {
            log.warn("Tentativa de login de usuário inativo");
            throw new BusinessException("Usuário inativo. Entre em contato com o administrador.");
        }

        if (!passwordEncoder.matches(request.senha(), usuario.getSenha())) {
            log.warn("Credenciais inválidas");
            throw new BusinessException("Credenciais inválidas");
        }

        String token = jwtTokenProvider.generateToken(usuario);
        long expiresIn = jwtTokenProvider.getExpirationTime();

        log.info("Login realizado com sucesso para usuário ID: {}", usuario.getId());

        return new LoginResponse(
            token,
            expiresIn,
            usuario.getId(),
            usuario.getNome(),
            usuario.getCpf(),
            usuario.getTipo().name()
        );
    }
}
