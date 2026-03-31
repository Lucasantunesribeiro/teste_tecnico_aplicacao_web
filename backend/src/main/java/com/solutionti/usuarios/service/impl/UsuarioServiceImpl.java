package com.solutionti.usuarios.service.impl;

import com.solutionti.usuarios.dto.request.UsuarioRequest;
import com.solutionti.usuarios.dto.response.UsuarioResponse;
import com.solutionti.usuarios.entity.Usuario;
import com.solutionti.usuarios.exception.BusinessException;
import com.solutionti.usuarios.exception.NotFoundException;
import com.solutionti.usuarios.exception.UnauthorizedException;
import com.solutionti.usuarios.mapper.UsuarioMapper;
import com.solutionti.usuarios.repository.UsuarioRepository;
import com.solutionti.usuarios.security.SecurityUtils;
import com.solutionti.usuarios.service.UsuarioService;
import com.solutionti.usuarios.validator.CpfValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final CpfValidator cpfValidator;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UsuarioResponse criar(UsuarioRequest request) {
        log.info("Criando novo usuário com CPF: {}", request.cpf());

        if (!cpfValidator.isValid(request.cpf())) {
            throw new BusinessException("CPF inválido: " + request.cpf());
        }

        if (usuarioRepository.existsByCpf(request.cpf())) {
            throw new BusinessException("Já existe um usuário cadastrado com o CPF: " + request.cpf());
        }

        Usuario usuario = usuarioMapper.toEntity(request);
        usuario.setSenha(passwordEncoder.encode(request.senha()));

        Usuario saved = usuarioRepository.save(usuario);
        log.info("Usuário criado com ID: {}", saved.getId());

        return usuarioMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorId(UUID id, UUID currentUserId) {
        log.debug("Buscando usuário ID: {}", id);

        if (!SecurityUtils.isAdmin() && !SecurityUtils.isOwner(id)) {
            throw new UnauthorizedException("Acesso negado: você não tem permissão para visualizar este usuário");
        }

        Usuario usuario = findUsuarioOrThrow(id);
        return usuarioMapper.toResponse(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UsuarioResponse> listarTodos(Pageable pageable) {
        log.debug("Listando todos os usuários, página: {}", pageable.getPageNumber());

        if (!SecurityUtils.isAdmin()) {
            throw new UnauthorizedException("Acesso negado: apenas administradores podem listar todos os usuários");
        }

        return usuarioRepository.findAll(pageable)
            .map(usuarioMapper::toResponse);
    }

    @Override
    @Transactional
    public UsuarioResponse atualizar(UUID id, UsuarioRequest request, UUID currentUserId) {
        log.info("Atualizando usuário ID: {}", id);

        if (!SecurityUtils.isAdmin() && !SecurityUtils.isOwner(id)) {
            throw new UnauthorizedException("Acesso negado: você não tem permissão para atualizar este usuário");
        }

        Usuario usuario = findUsuarioOrThrow(id);

        if (!usuario.getCpf().equals(request.cpf())) {
            if (!cpfValidator.isValid(request.cpf())) {
                throw new BusinessException("CPF inválido: " + request.cpf());
            }
            if (usuarioRepository.existsByCpf(request.cpf())) {
                throw new BusinessException("Já existe um usuário cadastrado com o CPF: " + request.cpf());
            }
        }

        usuarioMapper.updateEntity(usuario, request);
        usuario.setSenha(passwordEncoder.encode(request.senha()));

        Usuario updated = usuarioRepository.save(usuario);
        log.info("Usuário ID: {} atualizado com sucesso", id);

        return usuarioMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deletar(UUID id) {
        log.info("Deletando usuário ID: {}", id);

        if (!SecurityUtils.isAdmin()) {
            throw new UnauthorizedException("Acesso negado: apenas administradores podem deletar usuários");
        }

        Usuario usuario = findUsuarioOrThrow(id);
        usuarioRepository.delete(usuario);
        log.info("Usuário ID: {} deletado com sucesso", id);
    }

    private Usuario findUsuarioOrThrow(UUID id) {
        return usuarioRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Usuário não encontrado com ID: " + id));
    }
}
