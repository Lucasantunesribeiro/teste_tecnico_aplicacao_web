package com.solutionti.usuarios.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;

@Schema(description = "Dados de endereco retornados pela consulta de CEP (ViaCEP)")
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

    @Schema(description = "Codigo IBGE do municipio")
    String ibge,

    @Schema(description = "Indica erro na consulta (CEP nao encontrado)")
    Boolean erro
) implements Serializable {}
