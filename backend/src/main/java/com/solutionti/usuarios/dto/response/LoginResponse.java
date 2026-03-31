package com.solutionti.usuarios.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Resposta de autenticação com token JWT")
public record LoginResponse(

    @Schema(description = "Token JWT de acesso")
    String token,

    @Schema(description = "Tempo de expiração do token em segundos")
    long expiresIn,

    @Schema(description = "ID do usuário autenticado")
    UUID userId,

    @Schema(description = "Nome do usuário autenticado")
    String nome,

    @Schema(description = "CPF do usuário autenticado")
    String cpf,

    @Schema(description = "Tipo/perfil do usuário")
    String tipo
) {}
