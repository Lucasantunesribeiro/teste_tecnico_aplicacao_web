package com.solutionti.usuarios.service;

import com.solutionti.usuarios.dto.request.LoginRequest;
import com.solutionti.usuarios.entity.Usuario;
import com.solutionti.usuarios.entity.enums.StatusUsuario;
import com.solutionti.usuarios.entity.enums.TipoUsuario;
import com.solutionti.usuarios.exception.UnauthorizedException;
import com.solutionti.usuarios.repository.UsuarioRepository;
import com.solutionti.usuarios.security.JwtTokenProvider;
import com.solutionti.usuarios.security.SecurityUtils;
import com.solutionti.usuarios.service.auth.AuthSessionResult;
import com.solutionti.usuarios.service.auth.RefreshSessionStore;
import com.solutionti.usuarios.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RefreshSessionStore refreshSessionStore;

    @InjectMocks
    private AuthServiceImpl authService;

    private Usuario usuarioAtivo;

    @BeforeEach
    void setUp() {
        usuarioAtivo = Usuario.builder()
            .id(UUID.randomUUID())
            .nome("Joao Silva")
            .cpf("52998224725")
            .dataNascimento(LocalDate.of(1990, 1, 1))
            .senha("$2a$10$hashedPassword")
            .tipo(TipoUsuario.USER)
            .status(StatusUsuario.ATIVO)
            .build();

        ReflectionTestUtils.setField(authService, "refreshTokenExpirationMs", 604800000L);
    }

    @Test
    void deveRealizarLoginComSucesso() {
        LoginRequest request = new LoginRequest("52998224725", "Senha123");

        when(usuarioRepository.findByCpfAndStatus("52998224725", StatusUsuario.ATIVO))
            .thenReturn(Optional.of(usuarioAtivo));
        when(passwordEncoder.matches("Senha123", usuarioAtivo.getSenha())).thenReturn(true);
        when(jwtTokenProvider.generateAccessToken(usuarioAtivo)).thenReturn("access-token");
        when(jwtTokenProvider.getAccessTokenExpirationSeconds()).thenReturn(900L);
        when(refreshSessionStore.create(eq(usuarioAtivo.getId()), any(Duration.class))).thenReturn("refresh-token");

        AuthSessionResult result = authService.login(request);

        assertThat(result.accessToken()).isEqualTo("access-token");
        assertThat(result.refreshToken()).isEqualTo("refresh-token");
        assertThat(result.response().nome()).isEqualTo("Joao Silva");
        assertThat(result.response().cpf()).isEqualTo("52998224725");
        assertThat(result.response().tipo()).isEqualTo("USER");
    }

    @Test
    void deveLancarExcecaoQuandoCpfNaoEncontrado() {
        LoginRequest request = new LoginRequest("99999999999", "Senha123");
        when(usuarioRepository.findByCpfAndStatus("99999999999", StatusUsuario.ATIVO))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
            .isInstanceOf(UnauthorizedException.class)
            .hasMessageContaining("Credenciais");
    }

    @Test
    void deveLancarExcecaoQuandoSenhaInvalida() {
        LoginRequest request = new LoginRequest("52998224725", "SenhaErrada");

        when(usuarioRepository.findByCpfAndStatus("52998224725", StatusUsuario.ATIVO))
            .thenReturn(Optional.of(usuarioAtivo));
        when(passwordEncoder.matches("SenhaErrada", usuarioAtivo.getSenha())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
            .isInstanceOf(UnauthorizedException.class)
            .hasMessageContaining("Credenciais");
    }

    @Test
    void deveRetornarUsuarioAtualNoEndpointMe() {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(usuarioAtivo.getId().toString(), "n/a", List.of())
        );

        when(usuarioRepository.findById(usuarioAtivo.getId())).thenReturn(Optional.of(usuarioAtivo));
        when(jwtTokenProvider.getAccessTokenExpirationSeconds()).thenReturn(900L);

        var response = authService.me();

        assertThat(SecurityUtils.getCurrentUserId()).isEqualTo(usuarioAtivo.getId());
        assertThat(response.userId()).isEqualTo(usuarioAtivo.getId());
        assertThat(response.nome()).isEqualTo("Joao Silva");
    }
}
