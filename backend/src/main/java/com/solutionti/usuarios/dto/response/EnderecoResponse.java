package com.solutionti.usuarios.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Dados do endereco")
public record EnderecoResponse(

    @Schema(description = "ID unico do endereco")
    UUID id,

    @Schema(description = "CEP do endereco")
    String cep,

    @Schema(description = "Logradouro (rua, avenida, etc.)")
    String logradouro,

    @Schema(description = "Numero do endereco")
    String numero,

    @Schema(description = "Complemento do endereco")
    String complemento,

    @Schema(description = "Bairro")
    String bairro,

    @Schema(description = "Cidade")
    String cidade,

    @Schema(description = "Estado (UF)")
    String estado,

    @Schema(description = "Indica se e o endereco principal")
    boolean principal,

    @Schema(description = "ID do usuario dono do endereco")
    UUID usuarioId,

    @Schema(description = "Nome do usuario dono do endereco")
    String usuarioNome,

    @Schema(description = "CPF do usuario dono do endereco")
    String usuarioCpf
) {}
