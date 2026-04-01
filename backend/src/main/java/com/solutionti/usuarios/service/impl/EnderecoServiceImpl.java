package com.solutionti.usuarios.service.impl;

import com.solutionti.usuarios.dto.request.AtualizarEnderecoRequest;
import com.solutionti.usuarios.dto.request.EnderecoRequest;
import com.solutionti.usuarios.dto.response.CepResponse;
import com.solutionti.usuarios.dto.response.EnderecoResponse;
import com.solutionti.usuarios.entity.Endereco;
import com.solutionti.usuarios.entity.Usuario;
import com.solutionti.usuarios.exception.ForbiddenException;
import com.solutionti.usuarios.exception.NotFoundException;
import com.solutionti.usuarios.mapper.EnderecoMapper;
import com.solutionti.usuarios.repository.EnderecoRepository;
import com.solutionti.usuarios.repository.UsuarioRepository;
import com.solutionti.usuarios.security.SecurityUtils;
import com.solutionti.usuarios.service.CepService;
import com.solutionti.usuarios.service.EnderecoService;
import jakarta.persistence.criteria.JoinType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnderecoServiceImpl implements EnderecoService {

    private final EnderecoRepository enderecoRepository;
    private final UsuarioRepository usuarioRepository;
    private final EnderecoMapper enderecoMapper;
    private final CepService cepService;

    @Override
    @Transactional
    public EnderecoResponse criar(UUID usuarioId, EnderecoRequest request) {
        log.info("Criando endereco para usuario ID: {}", usuarioId);

        verificarPermissaoUsuario(usuarioId);

        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new NotFoundException("Usuario nao encontrado"));

        CepResponse cepData = cepService.consultarCep(request.cep());

        Endereco endereco = enderecoMapper.toEntity(request);
        endereco.setUsuario(usuario);
        endereco.setLogradouro(cepData.logradouro() != null ? cepData.logradouro() : "");
        endereco.setBairro(cepData.bairro() != null ? cepData.bairro() : "");
        endereco.setCidade(cepData.localidade() != null ? cepData.localidade() : "");
        endereco.setEstado(cepData.uf() != null ? cepData.uf() : "");

        boolean isPrimeiroEndereco = !enderecoRepository.existsByUsuarioId(usuarioId);
        if (isPrimeiroEndereco || request.principal()) {
            enderecoRepository.desmarcarPrincipalPorUsuario(usuarioId);
            endereco.tornarPrincipal();
        }

        Endereco saved = enderecoRepository.save(endereco);
        log.info("Endereco criado com ID: {} para usuario ID: {}", saved.getId(), usuarioId);

        return enderecoMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnderecoResponse> listarPorUsuario(UUID usuarioId) {
        log.debug("Listando enderecos do usuario ID: {}", usuarioId);

        verificarPermissaoUsuario(usuarioId);

        if (!usuarioRepository.existsById(usuarioId)) {
            throw new NotFoundException("Usuario nao encontrado");
        }

        List<Endereco> enderecos = enderecoRepository.findByUsuarioIdOrderByPrincipalDesc(usuarioId);
        return enderecoMapper.toResponseList(enderecos);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EnderecoResponse> listarTodos(UUID usuarioId,
                                              Boolean principal,
                                              String cep,
                                              String cidade,
                                              String estado,
                                              Pageable pageable) {
        requireAdmin();
        Specification<Endereco> spec = buildSearchSpecification(usuarioId, principal, cep, cidade, estado);

        return enderecoRepository.findAll(spec, pageable)
            .map(enderecoMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public EnderecoResponse buscarPorId(UUID id) {
        log.debug("Buscando endereco ID: {}", id);

        Endereco endereco = findEnderecoOrThrow(id);
        verificarPermissaoUsuario(endereco.getUsuario().getId());

        return enderecoMapper.toResponse(endereco);
    }

    @Override
    @Transactional
    public EnderecoResponse atualizar(UUID id, AtualizarEnderecoRequest request) {
        log.info("Atualizando endereco ID: {}", id);

        Endereco endereco = findEnderecoOrThrow(id);
        verificarPermissaoUsuario(endereco.getUsuario().getId());

        if (request.cep() != null && !request.cep().isBlank()) {
            CepResponse cepData = cepService.consultarCep(request.cep());
            endereco.setCep(request.cep());
            endereco.setLogradouro(cepData.logradouro() != null ? cepData.logradouro() : "");
            endereco.setBairro(cepData.bairro() != null ? cepData.bairro() : "");
            endereco.setCidade(cepData.localidade() != null ? cepData.localidade() : "");
            endereco.setEstado(cepData.uf() != null ? cepData.uf() : "");
        }

        if (request.numero() != null && !request.numero().isBlank()) {
            endereco.setNumero(request.numero());
        }

        if (request.complemento() != null) {
            endereco.setComplemento(request.complemento());
        }

        Endereco updated = enderecoRepository.save(endereco);
        log.info("Endereco ID: {} atualizado com sucesso", id);

        return enderecoMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deletar(UUID id) {
        log.info("Deletando endereco ID: {}", id);

        Endereco endereco = findEnderecoOrThrow(id);
        UUID usuarioId = endereco.getUsuario().getId();
        verificarPermissaoUsuario(usuarioId);

        boolean eraPrincipal = endereco.isPrincipal();
        enderecoRepository.delete(endereco);

        if (eraPrincipal) {
            enderecoRepository.findFirstByUsuarioIdOrderByCreatedAtAsc(usuarioId)
                .ifPresent(maisAntigo -> {
                    maisAntigo.tornarPrincipal();
                    enderecoRepository.save(maisAntigo);
                    log.info("Endereco ID: {} promovido a principal apos exclusao", maisAntigo.getId());
                });
        }

        log.info("Endereco ID: {} deletado com sucesso", id);
    }

    @Override
    @Transactional
    public EnderecoResponse definirComoPrincipal(UUID id) {
        log.info("Definindo endereco ID: {} como principal", id);

        Endereco endereco = findEnderecoOrThrow(id);
        UUID usuarioId = endereco.getUsuario().getId();
        verificarPermissaoUsuario(usuarioId);

        enderecoRepository.findByUsuarioIdForUpdate(usuarioId);

        enderecoRepository.desmarcarPrincipalPorUsuario(usuarioId);
        endereco.tornarPrincipal();
        Endereco updated = enderecoRepository.save(endereco);

        log.info("Endereco ID: {} definido como principal para usuario ID: {}", id, usuarioId);
        return enderecoMapper.toResponse(updated);
    }

    private Endereco findEnderecoOrThrow(UUID id) {
        return enderecoRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Endereco nao encontrado"));
    }

    private void verificarPermissaoUsuario(UUID usuarioId) {
        if (!SecurityUtils.isAdmin() && !SecurityUtils.isOwner(usuarioId)) {
            log.warn("Acesso negado para operacao em endereco do usuario ID: {}", usuarioId);
            throw new ForbiddenException("Acesso negado: voce nao tem permissao para acessar dados deste usuario");
        }
    }

    private void requireAdmin() {
        if (!SecurityUtils.isAdmin()) {
            log.warn("Acesso negado para listagem global de enderecos: usuario sem perfil ADMIN");
            throw new ForbiddenException("Acesso negado: apenas administradores podem listar todos os enderecos");
        }
    }

    private Specification<Endereco> buildSearchSpecification(UUID usuarioId,
                                                             Boolean principal,
                                                             String cep,
                                                             String cidade,
                                                             String estado) {
        return (root, query, criteriaBuilder) -> {
            if (!Long.class.equals(query.getResultType()) && !long.class.equals(query.getResultType())) {
                root.fetch("usuario", JoinType.INNER);
                query.distinct(true);
            }

            var predicates = criteriaBuilder.conjunction();

            if (usuarioId != null) {
                predicates = criteriaBuilder.and(predicates,
                    criteriaBuilder.equal(root.get("usuario").get("id"), usuarioId));
            }

            if (principal != null) {
                predicates = criteriaBuilder.and(predicates,
                    criteriaBuilder.equal(root.get("principal"), principal));
            }

            if (StringUtils.hasText(cep)) {
                predicates = criteriaBuilder.and(predicates,
                    criteriaBuilder.equal(root.get("cep"), cep.trim()));
            }

            if (StringUtils.hasText(cidade)) {
                predicates = criteriaBuilder.and(predicates,
                    criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("cidade")),
                        "%" + cidade.trim().toLowerCase() + "%"
                    ));
            }

            if (StringUtils.hasText(estado)) {
                predicates = criteriaBuilder.and(predicates,
                    criteriaBuilder.equal(
                        criteriaBuilder.upper(root.get("estado")),
                        estado.trim().toUpperCase()
                    ));
            }

            return predicates;
        };
    }
}
