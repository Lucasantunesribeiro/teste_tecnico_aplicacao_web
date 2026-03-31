package com.solutionti.usuarios.security;

import com.solutionti.usuarios.entity.Usuario;
import com.solutionti.usuarios.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        log.debug("Carregando usuário pelo ID: {}", userId);

        Usuario usuario = usuarioRepository.findById(UUID.fromString(userId))
            .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com ID: " + userId));

        String role = switch (usuario.getTipo()) {
            case ADMIN -> "ROLE_ADMIN";
            case USER -> "ROLE_USER";
        };

        return new User(
            usuario.getId().toString(),
            usuario.getSenha(),
            List.of(new SimpleGrantedAuthority(role))
        );
    }
}
