package com.solutionti.usuarios.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(description = "Dados para criação ou atualização de usuário")
public record UsuarioRequest(

    @Schema(description = "Nome completo do usuário", example = "João da Silva")
    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
    String nome,

    @Schema(description = "CPF do usuário (somente dígitos)", example = "52998224725")
    @NotBlank(message = "CPF é obrigatório")
    @Pattern(regexp = "\\d{11}", message = "CPF deve conter exatamente 11 dígitos numéricos")
    String cpf,

    @Schema(description = "Data de nascimento do usuário", example = "1990-05-15")
    @NotNull(message = "Data de nascimento é obrigatória")
    @Past(message = "Data de nascimento deve ser no passado")
    LocalDate dataNascimento,

    @Schema(description = "Senha do usuário (mínimo 8 caracteres, com maiúscula, minúscula e número)", example = "Senha123")
    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 8, message = "Senha deve ter no mínimo 8 caracteres")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$",
        message = "Senha deve conter pelo menos uma letra maiúscula, uma minúscula e um número"
    )
    String senha
) {}
