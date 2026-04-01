package com.solutionti.usuarios.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Resposta de autenticacao ou sessao atual")
public record LoginResponse(

    @Schema(description = "Tempo de expiracao do access token em segundos")
    long expiresIn,

    @Schema(description = "ID do usuario autenticado")
    UUID userId,

    @Schema(description = "Nome do usuario autenticado")
    String nome,

    @Schema(description = "CPF do usuario autenticado")
    String cpf,

    @Schema(description = "Tipo/perfil do usuario")
    String tipo
) {}
