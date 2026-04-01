package com.solutionti.usuarios.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados de autenticação do usuário")
public record LoginRequest(

    @Schema(description = "CPF do usuário (somente dígitos)", example = "52998224725")
    @NotBlank(message = "CPF é obrigatório")
    @Pattern(regexp = "\\d{11}", message = "CPF deve conter exatamente 11 dígitos numéricos")
    String cpf,

    @Schema(description = "Senha do usuário", example = "senha123")
    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 8, message = "Senha deve ter no mínimo 8 caracteres")
    String senha
) {}
