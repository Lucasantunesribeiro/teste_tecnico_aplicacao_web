package com.solutionti.usuarios.service;

import com.solutionti.usuarios.dto.request.AtualizarEnderecoRequest;
import com.solutionti.usuarios.dto.request.EnderecoRequest;
import com.solutionti.usuarios.dto.response.EnderecoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface EnderecoService {

    EnderecoResponse criar(UUID usuarioId, EnderecoRequest request);

    List<EnderecoResponse> listarPorUsuario(UUID usuarioId);

    Page<EnderecoResponse> listarTodos(UUID usuarioId, Boolean principal, String cep, String cidade, String estado, Pageable pageable);

    EnderecoResponse buscarPorId(UUID id);

    EnderecoResponse atualizar(UUID id, AtualizarEnderecoRequest request);

    void deletar(UUID id);

    EnderecoResponse definirComoPrincipal(UUID id);
}
