package com.solutionti.usuarios.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

@Schema(description = "Dados para criação de endereço")
public record EnderecoRequest(

    @Schema(description = "ID do usuário dono do endereço")
    @NotNull(message = "ID do usuário é obrigatório")
    UUID usuarioId,

    @Schema(description = "CEP (somente dígitos)", example = "01310100")
    @NotBlank(message = "CEP é obrigatório")
    @Pattern(regexp = "\\d{8}", message = "CEP deve conter exatamente 8 dígitos numéricos")
    String cep,

    @Schema(description = "Número do endereço", example = "100")
    @NotBlank(message = "Número é obrigatório")
    String numero,

    @Schema(description = "Complemento do endereço", example = "Apto 42")
    String complemento,

    @Schema(description = "Indica se é o endereço principal", example = "true")
    boolean principal
) {}
