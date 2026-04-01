package com.solutionti.usuarios.service;

import com.solutionti.usuarios.dto.request.AtualizarEnderecoRequest;
import com.solutionti.usuarios.dto.request.EnderecoRequest;
import com.solutionti.usuarios.dto.response.EnderecoResponse;

import java.util.List;
import java.util.UUID;

public interface EnderecoService {

    EnderecoResponse criar(UUID usuarioId, EnderecoRequest request);

    List<EnderecoResponse> listarPorUsuario(UUID usuarioId);

    EnderecoResponse buscarPorId(UUID id);

    EnderecoResponse atualizar(UUID id, AtualizarEnderecoRequest request);

    void deletar(UUID id);

    EnderecoResponse definirComoPrincipal(UUID id);
}
