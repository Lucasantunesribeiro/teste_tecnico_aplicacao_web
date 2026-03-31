package com.solutionti.usuarios.service;

import com.solutionti.usuarios.dto.request.AtualizarEnderecoRequest;
import com.solutionti.usuarios.dto.request.EnderecoRequest;
import com.solutionti.usuarios.dto.response.EnderecoResponse;

import java.util.List;
import java.util.UUID;

public interface EnderecoService {

    EnderecoResponse criar(UUID usuarioId, EnderecoRequest request, UUID currentUserId);

    List<EnderecoResponse> listarPorUsuario(UUID usuarioId, UUID currentUserId);

    EnderecoResponse buscarPorId(UUID id, UUID currentUserId);

    EnderecoResponse atualizar(UUID id, AtualizarEnderecoRequest request, UUID currentUserId);

    void deletar(UUID id, UUID currentUserId);

    EnderecoResponse definirComoPrincipal(UUID id, UUID currentUserId);
}
