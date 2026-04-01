package com.solutionti.usuarios.service;

import com.solutionti.usuarios.dto.request.AlterarSenhaRequest;
import com.solutionti.usuarios.dto.request.AtualizarUsuarioRequest;
import com.solutionti.usuarios.dto.request.UsuarioRequest;
import com.solutionti.usuarios.dto.response.UsuarioResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UsuarioService {

    UsuarioResponse criar(UsuarioRequest request);

    UsuarioResponse buscarPorId(UUID id);

    Page<UsuarioResponse> listarTodos(Pageable pageable);

    UsuarioResponse atualizar(UUID id, AtualizarUsuarioRequest request);

    void alterarSenha(UUID id, AlterarSenhaRequest request);

    void deletar(UUID id);
}
