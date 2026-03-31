package com.solutionti.usuarios.service;

import com.solutionti.usuarios.dto.request.LoginRequest;
import com.solutionti.usuarios.dto.response.LoginResponse;
import com.solutionti.usuarios.entity.Usuario;
import com.solutionti.usuarios.entity.enums.StatusUsuario;
import com.solutionti.usuarios.entity.enums.TipoUsuario;
import com.solutionti.usuarios.exception.BusinessException;
import com.solutionti.usuarios.exception.NotFoundException;
import com.solutionti.usuarios.repository.UsuarioRepository;
import com.solutionti.usuarios.security.JwtTokenProvider;
import com.solutionti.usuarios.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    private Usuario usuarioAtivo;
    private Usuario usuarioInativo;

    @BeforeEach
    void setUp() {
        usuarioAtivo = Usuario.builder()
            .id(UUID.randomUUID())
            .nome("João Silva")
            .cpf("52998224725")
            .dataNascimento(LocalDate.of(1990, 1, 1))
            .senha("$2a$10$hashedPassword")
            .tipo(TipoUsuario.USER)
            .status(StatusUsuario.ATIVO)
            .build();

        usuarioInativo = Usuario.builder()
            .id(UUID.randomUUID())
            .nome("Maria Inativa")
            .cpf("11144477735")
            .dataNascimento(LocalDate.of(1985, 6, 15))
            .senha("$2a$10$hashedPassword")
            .tipo(TipoUsuario.USER)
            .status(StatusUsuario.INATIVO)
            .build();
    }

    @Test
    void deveRealizarLoginComSucesso() {
        // Given
        LoginRequest request = new LoginRequest("52998224725", "senha123");
        String expectedToken = "eyJhbGciOiJIUzUxMiJ9.token";

        when(usuarioRepository.findByCpf("52998224725")).thenReturn(Optional.of(usuarioAtivo));
        when(passwordEncoder.matches("senha123", usuarioAtivo.getSenha())).thenReturn(true);
        when(jwtTokenProvider.generateToken(usuarioAtivo)).thenReturn(expectedToken);
        when(jwtTokenProvider.getExpirationTime()).thenReturn(86400000L);

        // When
        LoginResponse response = authService.login(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo(expectedToken);
        assertThat(response.nome()).isEqualTo("João Silva");
        assertThat(response.cpf()).isEqualTo("52998224725");
        assertThat(response.tipo()).isEqualTo("USER");
    }

    @Test
    void deveLancarExcecaoQuandoCpfNaoEncontrado() {
        // Given
        LoginRequest request = new LoginRequest("99999999999", "senha123");
        when(usuarioRepository.findByCpf("99999999999")).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> authService.login(request))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioInativo() {
        // Given
        LoginRequest request = new LoginRequest("11144477735", "senha123");
        when(usuarioRepository.findByCpf("11144477735")).thenReturn(Optional.of(usuarioInativo));

        // When/Then
        assertThatThrownBy(() -> authService.login(request))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("inativo");
    }

    @Test
    void deveLancarExcecaoQuandoSenhaInvalida() {
        // Given
        LoginRequest request = new LoginRequest("52998224725", "senhaErrada");

        when(usuarioRepository.findByCpf("52998224725")).thenReturn(Optional.of(usuarioAtivo));
        when(passwordEncoder.matches("senhaErrada", usuarioAtivo.getSenha())).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> authService.login(request))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Credenciais inválidas");
    }
}
