package com.solutionti.usuarios.controller;

import com.solutionti.usuarios.dto.request.AtualizarEnderecoRequest;
import com.solutionti.usuarios.dto.request.EnderecoRequest;
import com.solutionti.usuarios.dto.response.EnderecoResponse;
import com.solutionti.usuarios.dto.response.ErrorResponse;
import com.solutionti.usuarios.dto.response.PageResponse;
import com.solutionti.usuarios.service.EnderecoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
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
@Tag(name = "Enderecos", description = "Gerenciamento de endereços de usuários")
@SecurityRequirement(name = "cookieAuth")
public class EnderecoController {

    private final EnderecoService enderecoService;

    @PostMapping
    @Operation(summary = "Criar endereço", description = "Cria um novo endereço para um usuário")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Endereço criado com sucesso",
            content = @Content(schema = @Schema(implementation = EnderecoResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou CEP não encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Acesso negado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EnderecoResponse> criar(@RequestBody @Valid EnderecoRequest request) {
        log.info("Requisição para criar endereço para usuário ID: {}", request.usuarioId());
        EnderecoResponse response = enderecoService.criar(request.usuarioId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(
        summary = "Listar todos os endereços (ADMIN)",
        description = "Lista endereços paginados com filtros opcionais. Requer papel ADMIN. " +
                      "Parâmetros de paginação: page (0-based), size, sort (ex: cidade,asc)."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso",
            content = @Content(schema = @Schema(implementation = PageResponse.class))),
        @ApiResponse(responseCode = "403", description = "Acesso negado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PageResponse<EnderecoResponse>> listarTodos(
            @Parameter(description = "Filtrar por ID do usuário") @RequestParam(required = false) UUID usuarioId,
            @Parameter(description = "Filtrar por endereço principal") @RequestParam(required = false) Boolean principal,
            @Parameter(description = "Filtrar por CEP") @RequestParam(required = false) String cep,
            @Parameter(description = "Filtrar por cidade") @RequestParam(required = false) String cidade,
            @Parameter(description = "Filtrar por estado (sigla, ex: SP)") @RequestParam(required = false) String estado,
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(
            PageResponse.from(enderecoService.listarTodos(usuarioId, principal, cep, cidade, estado, pageable))
        );
    }

    @GetMapping("/usuario/{usuarioId}")
    @Operation(summary = "Listar endereços por usuário", description = "Lista todos os endereços de um usuário (ADMIN ou próprio usuário)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
        @ApiResponse(responseCode = "403", description = "Acesso negado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<EnderecoResponse>> listarPorUsuario(
            @Parameter(description = "ID do usuário", required = true) @PathVariable UUID usuarioId) {
        log.debug("Requisição para listar endereços do usuário ID: {}", usuarioId);
        return ResponseEntity.ok(enderecoService.listarPorUsuario(usuarioId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar endereço por ID", description = "Busca um endereço pelo ID (ADMIN ou próprio usuário)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Endereço encontrado",
            content = @Content(schema = @Schema(implementation = EnderecoResponse.class))),
        @ApiResponse(responseCode = "403", description = "Acesso negado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Endereço não encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EnderecoResponse> buscarPorId(
            @Parameter(description = "ID do endereço", required = true) @PathVariable UUID id) {
        log.debug("Requisição para buscar endereço ID: {}", id);
        return ResponseEntity.ok(enderecoService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar endereço", description = "Atualiza os dados de um endereço (ADMIN ou próprio usuário)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Endereço atualizado com sucesso",
            content = @Content(schema = @Schema(implementation = EnderecoResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Acesso negado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Endereço não encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EnderecoResponse> atualizar(
            @Parameter(description = "ID do endereço", required = true) @PathVariable UUID id,
            @RequestBody @Valid AtualizarEnderecoRequest request) {
        log.info("Requisição para atualizar endereço ID: {}", id);
        return ResponseEntity.ok(enderecoService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar endereço", description = "Remove um endereço do sistema (ADMIN ou próprio usuário)")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Endereço deletado com sucesso"),
        @ApiResponse(responseCode = "403", description = "Acesso negado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Endereço não encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deletar(
            @Parameter(description = "ID do endereço", required = true) @PathVariable UUID id) {
        log.info("Requisição para deletar endereço ID: {}", id);
        enderecoService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/principal")
    @Operation(summary = "Definir endereço principal", description = "Define um endereço como o principal do usuário (ADMIN ou próprio usuário)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Endereço definido como principal",
            content = @Content(schema = @Schema(implementation = EnderecoResponse.class))),
        @ApiResponse(responseCode = "403", description = "Acesso negado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Endereço não encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EnderecoResponse> definirComoPrincipal(
            @Parameter(description = "ID do endereço", required = true) @PathVariable UUID id) {
        log.info("Requisição para definir endereço ID: {} como principal", id);
        return ResponseEntity.ok(enderecoService.definirComoPrincipal(id));
    }
}
