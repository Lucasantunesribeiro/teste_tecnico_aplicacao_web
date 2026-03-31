package com.solutionti.usuarios.dto.response;

import com.solutionti.usuarios.entity.enums.StatusUsuario;
import com.solutionti.usuarios.entity.enums.TipoUsuario;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Dados do usuário")
public record UsuarioResponse(

    @Schema(description = "ID único do usuário")
    UUID id,

    @Schema(description = "Nome completo do usuário")
    String nome,

    @Schema(description = "CPF do usuário")
    String cpf,

    @Schema(description = "Data de nascimento do usuário")
    LocalDate dataNascimento,

    @Schema(description = "Tipo/perfil do usuário")
    TipoUsuario tipo,

    @Schema(description = "Status do usuário")
    StatusUsuario status
) {}
