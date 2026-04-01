package com.solutionti.usuarios.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Resposta de erro padronizada")
public record ErrorResponse(

    @Schema(description = "Momento em que o erro ocorreu")
    LocalDateTime timestamp,

    @Schema(description = "Codigo HTTP do erro")
    int status,

    @Schema(description = "Tipo do erro")
    String error,

    @Schema(description = "Mensagem descritiva do erro")
    String message,

    @Schema(description = "Caminho da requisicao que gerou o erro")
    String path,

    @Schema(description = "Correlation/request id da requisicao")
    String requestId,

    @Schema(description = "Lista de erros de campo (validacao)")
    List<FieldError> fieldErrors
) {

    @Schema(description = "Erro de validacao em campo especifico")
    public record FieldError(

        @Schema(description = "Nome do campo com erro")
        String field,

        @Schema(description = "Mensagem de erro do campo")
        String message
    ) {}
}
