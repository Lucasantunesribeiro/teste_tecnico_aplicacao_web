package com.solutionti.usuarios.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para alteração de senha. USER deve informar a senha atual; ADMIN pode omiti-la.")
public record AlterarSenhaRequest(

    @Schema(description = "Senha atual do usuário. Obrigatória para USER; ignorada para ADMIN.", example = "SenhaAtual1")
    String senhaAtual,

    @Schema(description = "Nova senha (mínimo 8 caracteres, com maiúscula, minúscula e número)", example = "NovaSenha1")
    @NotBlank(message = "Nova senha é obrigatória")
    @Size(min = 8, message = "Senha deve ter no mínimo 8 caracteres")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$",
        message = "Senha deve conter pelo menos uma letra maiúscula, uma minúscula e um número"
    )
    String novaSenha
) {}
