package com.solutionti.usuarios.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Resposta de erro padronizada")
public record ErrorResponse(

    @Schema(description = "Momento em que o erro ocorreu")
    LocalDateTime timestamp,

    @Schema(description = "Código HTTP do erro")
    int status,

    @Schema(description = "Tipo do erro")
    String error,

    @Schema(description = "Mensagem descritiva do erro")
    String message,

    @Schema(description = "Caminho da requisição que gerou o erro")
    String path,

    @Schema(description = "Lista de erros de campo (validação)")
    List<FieldError> fieldErrors
) {

    @Schema(description = "Erro de validação em campo específico")
    public record FieldError(

        @Schema(description = "Nome do campo com erro")
        String field,

        @Schema(description = "Mensagem de erro do campo")
        String message
    ) {}
}
