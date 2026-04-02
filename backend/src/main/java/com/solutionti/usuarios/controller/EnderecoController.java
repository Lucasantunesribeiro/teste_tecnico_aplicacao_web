package com.solutionti.usuarios.controller;

import com.solutionti.usuarios.config.OpenApiConfig;
import com.solutionti.usuarios.dto.request.AtualizarEnderecoRequest;
import com.solutionti.usuarios.dto.request.EnderecoRequest;
import com.solutionti.usuarios.dto.response.EnderecoResponse;
import com.solutionti.usuarios.dto.response.ErrorResponse;
import com.solutionti.usuarios.dto.response.PageResponse;
import com.solutionti.usuarios.service.EnderecoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
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
@Tag(name = "Enderecos", description = "Gerenciamento de enderecos de usuarios")
@SecurityRequirement(name = "cookieAuth")
public class EnderecoController {

    private final EnderecoService enderecoService;

    @PostMapping
    @Operation(
        summary = "Criar endereco",
        description = "Cria um novo endereco para um usuario",
        parameters = {
            @Parameter(
                name = OpenApiConfig.CSRF_HEADER_NAME,
                in = ParameterIn.HEADER,
                required = true,
                description = OpenApiConfig.CSRF_HEADER_DESCRIPTION
            )
        }
    )
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
    @Operation(
        summary = "Listar todos os enderecos (ADMIN)",
        description = "Lista enderecos paginados com filtros opcionais. Requer papel ADMIN. "
            + "Parametros de paginacao: page (0-based), size, sort (ex: cidade,asc)."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso",
            content = @Content(schema = @Schema(implementation = PageResponse.class))),
        @ApiResponse(responseCode = "403", description = "Acesso negado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PageResponse<EnderecoResponse>> listarTodos(
            @Parameter(description = "Filtrar por ID do usuario") @RequestParam(required = false) UUID usuarioId,
            @Parameter(description = "Filtrar por endereco principal") @RequestParam(required = false) Boolean principal,
            @Parameter(description = "Filtrar por CEP") @RequestParam(required = false) String cep,
            @Parameter(description = "Filtrar por cidade") @RequestParam(required = false) String cidade,
            @Parameter(description = "Filtrar por estado (sigla, ex: SP)") @RequestParam(required = false) String estado,
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(
            PageResponse.from(enderecoService.listarTodos(usuarioId, principal, cep, cidade, estado, pageable))
        );
    }

    @GetMapping("/usuario/{usuarioId}")
    @Operation(summary = "Listar enderecos por usuario", description = "Lista todos os enderecos de um usuario (ADMIN ou proprio usuario)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
        @ApiResponse(responseCode = "403", description = "Acesso negado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Usuario nao encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<EnderecoResponse>> listarPorUsuario(
            @Parameter(description = "ID do usuario", required = true) @PathVariable UUID usuarioId) {
        log.debug("Requisicao para listar enderecos do usuario ID: {}", usuarioId);
        return ResponseEntity.ok(enderecoService.listarPorUsuario(usuarioId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar endereco por ID", description = "Busca um endereco pelo ID (ADMIN ou proprio usuario)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Endereco encontrado",
            content = @Content(schema = @Schema(implementation = EnderecoResponse.class))),
        @ApiResponse(responseCode = "403", description = "Acesso negado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Endereco nao encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EnderecoResponse> buscarPorId(
            @Parameter(description = "ID do endereco", required = true) @PathVariable UUID id) {
        log.debug("Requisicao para buscar endereco ID: {}", id);
        return ResponseEntity.ok(enderecoService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Atualizar endereco",
        description = "Atualiza os dados de um endereco (ADMIN ou proprio usuario)",
        parameters = {
            @Parameter(
                name = OpenApiConfig.CSRF_HEADER_NAME,
                in = ParameterIn.HEADER,
                required = true,
                description = OpenApiConfig.CSRF_HEADER_DESCRIPTION
            )
        }
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Endereco atualizado com sucesso",
            content = @Content(schema = @Schema(implementation = EnderecoResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dados invalidos",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Acesso negado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Endereco nao encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EnderecoResponse> atualizar(
            @Parameter(description = "ID do endereco", required = true) @PathVariable UUID id,
            @RequestBody @Valid AtualizarEnderecoRequest request) {
        log.info("Requisicao para atualizar endereco ID: {}", id);
        return ResponseEntity.ok(enderecoService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Deletar endereco",
        description = "Remove um endereco do sistema (ADMIN ou proprio usuario)",
        parameters = {
            @Parameter(
                name = OpenApiConfig.CSRF_HEADER_NAME,
                in = ParameterIn.HEADER,
                required = true,
                description = OpenApiConfig.CSRF_HEADER_DESCRIPTION
            )
        }
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Endereco deletado com sucesso"),
        @ApiResponse(responseCode = "403", description = "Acesso negado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Endereco nao encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deletar(
            @Parameter(description = "ID do endereco", required = true) @PathVariable UUID id) {
        log.info("Requisicao para deletar endereco ID: {}", id);
        enderecoService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/principal")
    @Operation(
        summary = "Definir endereco principal",
        description = "Define um endereco como o principal do usuario (ADMIN ou proprio usuario)",
        parameters = {
            @Parameter(
                name = OpenApiConfig.CSRF_HEADER_NAME,
                in = ParameterIn.HEADER,
                required = true,
                description = OpenApiConfig.CSRF_HEADER_DESCRIPTION
            )
        }
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Endereco definido como principal",
            content = @Content(schema = @Schema(implementation = EnderecoResponse.class))),
        @ApiResponse(responseCode = "403", description = "Acesso negado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Endereco nao encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EnderecoResponse> definirComoPrincipal(
            @Parameter(description = "ID do endereco", required = true) @PathVariable UUID id) {
        log.info("Requisicao para definir endereco ID: {} como principal", id);
        return ResponseEntity.ok(enderecoService.definirComoPrincipal(id));
    }
}
