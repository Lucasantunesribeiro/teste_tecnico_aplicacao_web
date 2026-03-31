package com.solutionti.usuarios.security;

import com.solutionti.usuarios.repository.EnderecoRepository;
import com.solutionti.usuarios.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service("securityService")
@RequiredArgsConstructor
@Slf4j
public class SecurityService {

    private final UsuarioRepository usuarioRepository;
    private final EnderecoRepository enderecoRepository;

    public boolean isOwner(UUID usuarioId) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) return false;
        return currentUserId.equals(usuarioId);
    }

    public boolean isEnderecoOwner(UUID enderecoId) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) return false;
        return enderecoRepository.findById(enderecoId)
            .map(endereco -> currentUserId.equals(endereco.getUsuario().getId()))
            .orElse(false);
    }
}
