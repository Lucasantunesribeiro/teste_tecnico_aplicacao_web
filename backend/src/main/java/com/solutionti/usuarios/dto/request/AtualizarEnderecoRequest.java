package com.solutionti.usuarios.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Dados para atualização parcial de endereço")
public record AtualizarEnderecoRequest(

    @Schema(description = "CEP (somente dígitos)", example = "01310100")
    @Pattern(regexp = "\\d{8}", message = "CEP deve conter exatamente 8 dígitos numéricos")
    String cep,

    @Schema(description = "Número do endereço", example = "200")
    String numero,

    @Schema(description = "Complemento do endereço", example = "Sala 5")
    String complemento
) {}
