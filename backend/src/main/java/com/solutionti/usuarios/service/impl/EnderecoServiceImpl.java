package com.solutionti.usuarios.service.impl;

import com.solutionti.usuarios.dto.request.AtualizarEnderecoRequest;
import com.solutionti.usuarios.dto.request.EnderecoRequest;
import com.solutionti.usuarios.dto.response.CepResponse;
import com.solutionti.usuarios.dto.response.EnderecoResponse;
import com.solutionti.usuarios.entity.Endereco;
import com.solutionti.usuarios.entity.Usuario;
import com.solutionti.usuarios.exception.NotFoundException;
import com.solutionti.usuarios.exception.UnauthorizedException;
import com.solutionti.usuarios.mapper.EnderecoMapper;
import com.solutionti.usuarios.repository.EnderecoRepository;
import com.solutionti.usuarios.repository.UsuarioRepository;
import com.solutionti.usuarios.security.SecurityUtils;
import com.solutionti.usuarios.service.CepService;
import com.solutionti.usuarios.service.EnderecoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public EnderecoResponse criar(UUID usuarioId, EnderecoRequest request, UUID currentUserId) {
        log.info("Criando endereço para usuário ID: {}", usuarioId);

        verificarPermissaoUsuario(usuarioId);

        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new NotFoundException("Usuário não encontrado com ID: " + usuarioId));

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
        log.info("Endereço criado com ID: {} para usuário ID: {}", saved.getId(), usuarioId);

        return enderecoMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnderecoResponse> listarPorUsuario(UUID usuarioId, UUID currentUserId) {
        log.debug("Listando endereços do usuário ID: {}", usuarioId);

        verificarPermissaoUsuario(usuarioId);

        if (!usuarioRepository.existsById(usuarioId)) {
            throw new NotFoundException("Usuário não encontrado com ID: " + usuarioId);
        }

        List<Endereco> enderecos = enderecoRepository.findByUsuarioIdOrderByPrincipalDesc(usuarioId);
        return enderecoMapper.toResponseList(enderecos);
    }

    @Override
    @Transactional(readOnly = true)
    public EnderecoResponse buscarPorId(UUID id, UUID currentUserId) {
        log.debug("Buscando endereço ID: {}", id);

        Endereco endereco = findEnderecoOrThrow(id);
        verificarPermissaoUsuario(endereco.getUsuario().getId());

        return enderecoMapper.toResponse(endereco);
    }

    @Override
    @Transactional
    public EnderecoResponse atualizar(UUID id, AtualizarEnderecoRequest request, UUID currentUserId) {
        log.info("Atualizando endereço ID: {}", id);

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
        log.info("Endereço ID: {} atualizado com sucesso", id);

        return enderecoMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deletar(UUID id, UUID currentUserId) {
        log.info("Deletando endereço ID: {}", id);

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
                    log.info("Endereço ID: {} promovido a principal após exclusão", maisAntigo.getId());
                });
        }

        log.info("Endereço ID: {} deletado com sucesso", id);
    }

    @Override
    @Transactional
    public EnderecoResponse definirComoPrincipal(UUID id, UUID currentUserId) {
        log.info("Definindo endereço ID: {} como principal", id);

        Endereco endereco = findEnderecoOrThrow(id);
        UUID usuarioId = endereco.getUsuario().getId();
        verificarPermissaoUsuario(usuarioId);

        enderecoRepository.desmarcarPrincipalPorUsuario(usuarioId);
        endereco.tornarPrincipal();
        Endereco updated = enderecoRepository.save(endereco);

        log.info("Endereço ID: {} definido como principal para usuário ID: {}", id, usuarioId);
        return enderecoMapper.toResponse(updated);
    }

    private Endereco findEnderecoOrThrow(UUID id) {
        return enderecoRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Endereço não encontrado com ID: " + id));
    }

    private void verificarPermissaoUsuario(UUID usuarioId) {
        if (!SecurityUtils.isAdmin() && !SecurityUtils.isOwner(usuarioId)) {
            throw new UnauthorizedException("Acesso negado: você não tem permissão para acessar dados deste usuário");
        }
    }
}
