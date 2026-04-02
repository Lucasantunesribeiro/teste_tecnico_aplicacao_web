package com.solutionti.usuarios.service;

import com.solutionti.usuarios.dto.request.AlterarSenhaRequest;
import com.solutionti.usuarios.dto.request.AtualizarUsuarioRequest;
import com.solutionti.usuarios.dto.request.UsuarioRequest;
import com.solutionti.usuarios.dto.response.UsuarioResponse;
import com.solutionti.usuarios.entity.Usuario;
import com.solutionti.usuarios.entity.enums.StatusUsuario;
import com.solutionti.usuarios.entity.enums.TipoUsuario;
import com.solutionti.usuarios.exception.BusinessException;
import com.solutionti.usuarios.mapper.UsuarioMapper;
import com.solutionti.usuarios.repository.UsuarioRepository;
import com.solutionti.usuarios.service.impl.UsuarioServiceImpl;
import com.solutionti.usuarios.validator.CpfValidator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioMapper usuarioMapper;

    @Mock
    private CpfValidator cpfValidator;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    private UsuarioRequest request;
    private Usuario savedUsuario;
    private UsuarioResponse expectedResponse;

    @BeforeEach
    void setUp() {
        setAdminAuthentication();

        request = new UsuarioRequest(
            "João da Silva",
            "52998224725",
            LocalDate.of(1990, 5, 15),
            "senha123"
        );

        savedUsuario = Usuario.builder()
            .id(UUID.randomUUID())
            .nome("João da Silva")
            .cpf("52998224725")
            .dataNascimento(LocalDate.of(1990, 5, 15))
            .senha("$2a$10$encodedPassword")
            .tipo(TipoUsuario.USER)
            .status(StatusUsuario.ATIVO)
            .build();

        expectedResponse = new UsuarioResponse(
            savedUsuario.getId(),
            "João da Silva",
            "52998224725",
            LocalDate.of(1990, 5, 15),
            TipoUsuario.USER,
            StatusUsuario.ATIVO
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deveCriarUsuarioComSucesso() {
        // Given
        when(cpfValidator.isValid("52998224725")).thenReturn(true);
        when(usuarioRepository.existsByCpf("52998224725")).thenReturn(false);
        when(usuarioMapper.toEntity(request)).thenReturn(savedUsuario);
        when(passwordEncoder.encode("senha123")).thenReturn("$2a$10$encodedPassword");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(savedUsuario);
        when(usuarioMapper.toResponse(savedUsuario)).thenReturn(expectedResponse);

        // When
        UsuarioResponse response = usuarioService.criar(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.nome()).isEqualTo("João da Silva");
        assertThat(response.cpf()).isEqualTo("52998224725");
        assertThat(response.tipo()).isEqualTo(TipoUsuario.USER);
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void deveLancarExcecaoParaCpfInvalido() {
        // Given
        when(cpfValidator.isValid("52998224725")).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> usuarioService.criar(request))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("CPF inválido");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoParaCpfDuplicado() {
        // Given
        when(cpfValidator.isValid("52998224725")).thenReturn(true);
        when(usuarioRepository.existsByCpf("52998224725")).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> usuarioService.criar(request))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("CPF");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoEhAdministrador() {
        // Given
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(
                UUID.randomUUID().toString(),
                "senha",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
            )
        );

        // When/Then
        assertThatThrownBy(() -> usuarioService.criar(request))
            .isInstanceOf(com.solutionti.usuarios.exception.ForbiddenException.class)
            .hasMessageContaining("apenas administradores");
    }

    @Test
    void deveAtualizarUsuarioSemAlterarSenhaQuandoSenhaOmitida() {
        // Given
        AtualizarUsuarioRequest updateRequest = new AtualizarUsuarioRequest(
            "João Atualizado",
            "52998224725",
            LocalDate.of(1990, 5, 15),
            null
        );
        when(usuarioRepository.findById(savedUsuario.getId())).thenReturn(java.util.Optional.of(savedUsuario));
        doNothing().when(usuarioMapper).updateEntity(any(Usuario.class), any(AtualizarUsuarioRequest.class));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(savedUsuario);
        when(usuarioMapper.toResponse(savedUsuario)).thenReturn(expectedResponse);

        // When
        usuarioService.atualizar(savedUsuario.getId(), updateRequest);

        // Then — password encoder must NOT be called when senha is null/blank
        verify(passwordEncoder, never()).encode(anyString());
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void deveAtualizarSenhaQuandoSenhaFornecida() {
        // Given
        AtualizarUsuarioRequest updateRequest = new AtualizarUsuarioRequest(
            "João da Silva",
            "52998224725",
            LocalDate.of(1990, 5, 15),
            "NovaSenha1"
        );
        when(usuarioRepository.findById(savedUsuario.getId())).thenReturn(java.util.Optional.of(savedUsuario));
        doNothing().when(usuarioMapper).updateEntity(any(Usuario.class), any(AtualizarUsuarioRequest.class));
        when(passwordEncoder.encode("NovaSenha1")).thenReturn("$2a$10$newEncodedPassword");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(savedUsuario);
        when(usuarioMapper.toResponse(savedUsuario)).thenReturn(expectedResponse);

        // When
        usuarioService.atualizar(savedUsuario.getId(), updateRequest);

        // Then
        verify(passwordEncoder).encode("NovaSenha1");
    }

    @Test
    void deveAlterarSenhaComoAdmin() {
        // Given
        AlterarSenhaRequest senhaRequest = new AlterarSenhaRequest(null, "NovaSenha1");
        when(usuarioRepository.findById(savedUsuario.getId())).thenReturn(java.util.Optional.of(savedUsuario));
        when(passwordEncoder.encode("NovaSenha1")).thenReturn("$2a$10$encodedNew");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(savedUsuario);

        // When
        usuarioService.alterarSenha(savedUsuario.getId(), senhaRequest);

        // Then — admin skips senhaAtual check
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(passwordEncoder).encode("NovaSenha1");
    }

    @Test
    void deveAlterarSenhaComoUserComSenhaAtualCorreta() {
        // Given
        UUID userId = savedUsuario.getId();
        setUserAuthentication(userId);

        AlterarSenhaRequest senhaRequest = new AlterarSenhaRequest("SenhaAtual1", "NovaSenha1");
        when(usuarioRepository.findById(userId)).thenReturn(java.util.Optional.of(savedUsuario));
        when(passwordEncoder.matches("SenhaAtual1", savedUsuario.getSenha())).thenReturn(true);
        when(passwordEncoder.encode("NovaSenha1")).thenReturn("$2a$10$encodedNew");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(savedUsuario);

        // When
        usuarioService.alterarSenha(userId, senhaRequest);

        // Then — use the original encoded password, not savedUsuario.getSenha() which
        // is mutated by the service call (setSenha is called with the new encoded value)
        verify(passwordEncoder).matches("SenhaAtual1", "$2a$10$encodedPassword");
        verify(passwordEncoder).encode("NovaSenha1");
    }

    @Test
    void deveLancarExcecaoQuandoSenhaAtualIncorreta() {
        // Given
        UUID userId = savedUsuario.getId();
        setUserAuthentication(userId);

        AlterarSenhaRequest senhaRequest = new AlterarSenhaRequest("SenhaErrada1", "NovaSenha1");
        when(usuarioRepository.findById(userId)).thenReturn(java.util.Optional.of(savedUsuario));
        when(passwordEncoder.matches("SenhaErrada1", savedUsuario.getSenha())).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> usuarioService.alterarSenha(userId, senhaRequest))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Senha atual incorreta");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void deveLancarForbiddenQuandoUserTentaAlterarSenhaDeOutro() {
        // Given
        UUID outroId = UUID.randomUUID();
        setUserAuthentication(UUID.randomUUID()); // authenticated as a different user

        AlterarSenhaRequest senhaRequest = new AlterarSenhaRequest("SenhaAtual1", "NovaSenha1");
        when(usuarioRepository.findById(outroId)).thenReturn(java.util.Optional.of(savedUsuario));

        // When/Then
        assertThatThrownBy(() -> usuarioService.alterarSenha(outroId, senhaRequest))
            .isInstanceOf(com.solutionti.usuarios.exception.ForbiddenException.class);
    }

    private void setAdminAuthentication() {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(
                UUID.randomUUID().toString(),
                "senha",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
            )
        );
    }

    private void setUserAuthentication(UUID userId) {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(
                userId.toString(),
                "senha",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
            )
        );
    }
}
