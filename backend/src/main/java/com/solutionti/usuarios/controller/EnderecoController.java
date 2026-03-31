package com.solutionti.usuarios.controller;

import com.solutionti.usuarios.dto.request.AtualizarEnderecoRequest;
import com.solutionti.usuarios.dto.request.EnderecoRequest;
import com.solutionti.usuarios.dto.response.EnderecoResponse;
import com.solutionti.usuarios.dto.response.ErrorResponse;
import com.solutionti.usuarios.security.SecurityUtils;
import com.solutionti.usuarios.service.EnderecoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/enderecos")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Enderecos", description = "Gerenciamento de endereços de usuários")
@SecurityRequirement(name = "bearerAuth")
public class EnderecoController {

    private final EnderecoService enderecoService;

    @PostMapping
    @Operation(summary = "Criar endereço", description = "Cria um novo endereço para um usuário")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Endereço criado com sucesso",
            content = @Content(schema = @Schema(implementation = EnderecoResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou CEP não encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EnderecoResponse> criar(@RequestBody @Valid EnderecoRequest request) {
        log.info("Requisição para criar endereço para usuário ID: {}", request.usuarioId());
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        EnderecoResponse response = enderecoService.criar(request.usuarioId(), request, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/usuario/{usuarioId}")
    @Operation(summary = "Listar endereços por usuário", description = "Lista todos os endereços de um usuário")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<EnderecoResponse>> listarPorUsuario(@PathVariable UUID usuarioId) {
        log.debug("Requisição para listar endereços do usuário ID: {}", usuarioId);
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        List<EnderecoResponse> response = enderecoService.listarPorUsuario(usuarioId, currentUserId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar endereço por ID", description = "Busca um endereço pelo ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Endereço encontrado",
            content = @Content(schema = @Schema(implementation = EnderecoResponse.class))),
        @ApiResponse(responseCode = "404", description = "Endereço não encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EnderecoResponse> buscarPorId(@PathVariable UUID id) {
        log.debug("Requisição para buscar endereço ID: {}", id);
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        EnderecoResponse response = enderecoService.buscarPorId(id, currentUserId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar endereço", description = "Atualiza os dados de um endereço")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Endereço atualizado com sucesso",
            content = @Content(schema = @Schema(implementation = EnderecoResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Endereço não encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EnderecoResponse> atualizar(@PathVariable UUID id,
                                                       @RequestBody @Valid AtualizarEnderecoRequest request) {
        log.info("Requisição para atualizar endereço ID: {}", id);
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        EnderecoResponse response = enderecoService.atualizar(id, request, currentUserId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar endereço", description = "Remove um endereço do sistema")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Endereço deletado com sucesso"),
        @ApiResponse(responseCode = "401", description = "Não autorizado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Endereço não encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deletar(@PathVariable UUID id) {
        log.info("Requisição para deletar endereço ID: {}", id);
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        enderecoService.deletar(id, currentUserId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/principal")
    @Operation(summary = "Definir endereço principal", description = "Define um endereço como o principal do usuário")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Endereço definido como principal",
            content = @Content(schema = @Schema(implementation = EnderecoResponse.class))),
        @ApiResponse(responseCode = "401", description = "Não autorizado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Endereço não encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EnderecoResponse> definirComoPrincipal(@PathVariable UUID id) {
        log.info("Requisição para definir endereço ID: {} como principal", id);
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        EnderecoResponse response = enderecoService.definirComoPrincipal(id, currentUserId);
        return ResponseEntity.ok(response);
    }
}
