package com.solutionti.usuarios.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados de endereço retornados pela consulta de CEP (ViaCEP)")
public record CepResponse(

    @Schema(description = "CEP consultado")
    String cep,

    @Schema(description = "Logradouro")
    String logradouro,

    @Schema(description = "Complemento do logradouro")
    String complemento,

    @Schema(description = "Bairro")
    String bairro,

    @Schema(description = "Localidade (cidade)")
    String localidade,

    @Schema(description = "UF (estado)")
    String uf,

    @Schema(description = "Código IBGE do município")
    String ibge,

    @Schema(description = "Indica erro na consulta (CEP não encontrado)")
    Boolean erro
) {}
