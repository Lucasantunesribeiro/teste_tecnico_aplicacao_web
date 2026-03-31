package com.solutionti.usuarios.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Dados do endereço")
public record EnderecoResponse(

    @Schema(description = "ID único do endereço")
    UUID id,

    @Schema(description = "CEP do endereço")
    String cep,

    @Schema(description = "Logradouro (rua, avenida, etc.)")
    String logradouro,

    @Schema(description = "Número do endereço")
    String numero,

    @Schema(description = "Complemento do endereço")
    String complemento,

    @Schema(description = "Bairro")
    String bairro,

    @Schema(description = "Cidade")
    String cidade,

    @Schema(description = "Estado (UF)")
    String estado,

    @Schema(description = "Indica se é o endereço principal")
    boolean principal,

    @Schema(description = "ID do usuário dono do endereço")
    UUID usuarioId
) {}
