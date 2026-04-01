package com.solutionti.usuarios.controller;

import com.solutionti.usuarios.dto.request.AtualizarEnderecoRequest;
import com.solutionti.usuarios.dto.request.EnderecoRequest;
import com.solutionti.usuarios.dto.response.EnderecoResponse;
import com.solutionti.usuarios.dto.response.ErrorResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/enderecos")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Enderecos", description = "Gerenciamento de enderecos de usuarios")
@SecurityRequirement(name = "bearerAuth")
public class EnderecoController {

    private final EnderecoService enderecoService;

    @PostMapping
    @Operation(summary = "Criar endereco", description = "Cria um novo endereco para um usuario")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Endereco criado com sucesso",
            content = @Content(schema = @Schema(implementation = EnderecoResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dados invalidos ou CEP nao encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Acesso negado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Usuario nao encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EnderecoResponse> criar(@RequestBody @Valid EnderecoRequest request) {
        log.info("Requisicao para criar endereco para usuario ID: {}", request.usuarioId());
        EnderecoResponse response = enderecoService.criar(request.usuarioId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar todos os enderecos", description = "Lista enderecos paginados para administradores")
    public ResponseEntity<Page<EnderecoResponse>> listarTodos(
            @RequestParam(required = false) UUID usuarioId,
            @RequestParam(required = false) Boolean principal,
            @RequestParam(required = false) String cep,
            @RequestParam(required = false) String cidade,
            @RequestParam(required = false) String estado,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        Page<EnderecoResponse> page = enderecoService.listarTodos(usuarioId, principal, cep, cidade, estado, pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/usuario/{usuarioId}")
    @Operation(summary = "Listar enderecos por usuario", description = "Lista todos os enderecos de um usuario")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
        @ApiResponse(responseCode = "403", description = "Acesso negado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Usuario nao encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<EnderecoResponse>> listarPorUsuario(@PathVariable UUID usuarioId) {
        log.debug("Requisicao para listar enderecos do usuario ID: {}", usuarioId);
        List<EnderecoResponse> response = enderecoService.listarPorUsuario(usuarioId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar endereco por ID", description = "Busca um endereco pelo ID")
    public ResponseEntity<EnderecoResponse> buscarPorId(@PathVariable UUID id) {
        log.debug("Requisicao para buscar endereco ID: {}", id);
        EnderecoResponse response = enderecoService.buscarPorId(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar endereco", description = "Atualiza os dados de um endereco")
    public ResponseEntity<EnderecoResponse> atualizar(@PathVariable UUID id,
                                                      @RequestBody @Valid AtualizarEnderecoRequest request) {
        log.info("Requisicao para atualizar endereco ID: {}", id);
        EnderecoResponse response = enderecoService.atualizar(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar endereco", description = "Remove um endereco do sistema")
    public ResponseEntity<Void> deletar(@PathVariable UUID id) {
        log.info("Requisicao para deletar endereco ID: {}", id);
        enderecoService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/principal")
    @Operation(summary = "Definir endereco principal", description = "Define um endereco como o principal do usuario")
    public ResponseEntity<EnderecoResponse> definirComoPrincipal(@PathVariable UUID id) {
        log.info("Requisicao para definir endereco ID: {} como principal", id);
        EnderecoResponse response = enderecoService.definirComoPrincipal(id);
        return ResponseEntity.ok(response);
    }
}
