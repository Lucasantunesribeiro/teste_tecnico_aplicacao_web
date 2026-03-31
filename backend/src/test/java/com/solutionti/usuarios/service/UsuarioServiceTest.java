package com.solutionti.usuarios.service;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
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
}
