package com.solutionti.usuarios.controller;

import com.solutionti.usuarios.config.OpenApiConfig;
import com.solutionti.usuarios.dto.request.AlterarSenhaRequest;
import com.solutionti.usuarios.dto.request.AtualizarUsuarioRequest;
import com.solutionti.usuarios.dto.request.UsuarioRequest;
import com.solutionti.usuarios.dto.response.ErrorResponse;
import com.solutionti.usuarios.dto.response.PageResponse;
import com.solutionti.usuarios.dto.response.UsuarioResponse;
import com.solutionti.usuarios.service.UsuarioService;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Usuarios", description = "Gerenciamento de usuarios")
@SecurityRequirement(name = "cookieAuth")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Criar usuario",
        description = "Cria um novo usuario no sistema (apenas ADMIN)",
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
        @ApiResponse(responseCode = "201", description = "Usuario criado com sucesso",
            content = @Content(schema = @Schema(implementation = UsuarioResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dados invalidos ou CPF ja cadastrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Nao autenticado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Acesso negado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<UsuarioResponse> criar(@RequestBody @Valid UsuarioRequest request) {
        log.info("Requisicao para criar usuario com CPF: {}", request.cpf());
        UsuarioResponse response = usuarioService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Listar usuarios",
        description = "Lista todos os usuarios paginados (apenas ADMIN). "
            + "Parametros de paginacao: page (0-based), size, sort (ex: nome,asc)."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso",
            content = @Content(schema = @Schema(implementation = PageResponse.class))),
        @ApiResponse(responseCode = "403", description = "Acesso negado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PageResponse<UsuarioResponse>> listar(
            @ParameterObject @PageableDefault(size = 20, sort = "nome") Pageable pageable) {
        log.debug("Requisicao para listar usuarios");
        return ResponseEntity.ok(PageResponse.from(usuarioService.listarTodos(pageable)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar usuario por ID", description = "Busca um usuario pelo ID (ADMIN ou proprio usuario)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Usuario encontrado",
            content = @Content(schema = @Schema(implementation = UsuarioResponse.class))),
        @ApiResponse(responseCode = "403", description = "Acesso negado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Usuario nao encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<UsuarioResponse> buscarPorId(
            @Parameter(description = "ID do usuario", required = true) @PathVariable UUID id) {
        log.debug("Requisicao para buscar usuario ID: {}", id);
        return ResponseEntity.ok(usuarioService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Atualizar usuario",
        description = "Atualiza os dados de um usuario (apenas ADMIN). "
            + "O campo 'senha' e opcional - omitir preserva a senha atual. "
            + "Para alterar somente a senha use PATCH /{id}/senha.",
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
        @ApiResponse(responseCode = "200", description = "Usuario atualizado com sucesso",
            content = @Content(schema = @Schema(implementation = UsuarioResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dados invalidos",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Acesso negado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Usuario nao encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<UsuarioResponse> atualizar(
            @Parameter(description = "ID do usuario", required = true) @PathVariable UUID id,
            @RequestBody @Valid AtualizarUsuarioRequest request) {
        log.info("Requisicao para atualizar usuario ID: {}", id);
        return ResponseEntity.ok(usuarioService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Deletar usuario",
        description = "Remove um usuario do sistema (apenas ADMIN)",
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
        @ApiResponse(responseCode = "204", description = "Usuario deletado com sucesso"),
        @ApiResponse(responseCode = "403", description = "Acesso negado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Usuario nao encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deletar(
            @Parameter(description = "ID do usuario", required = true) @PathVariable UUID id) {
        log.info("Requisicao para deletar usuario ID: {}", id);
        usuarioService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/senha")
    @Operation(
        summary = "Alterar senha",
        description = "USER altera a propria senha (campo `senhaAtual` obrigatorio). "
            + "ADMIN redefine qualquer senha sem confirmacao (`senhaAtual` pode ser null).",
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
        @ApiResponse(responseCode = "204", description = "Senha alterada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Senha atual incorreta ou nova senha invalida",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Acesso negado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Usuario nao encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> alterarSenha(
            @Parameter(description = "ID do usuario", required = true) @PathVariable UUID id,
            @RequestBody @Valid AlterarSenhaRequest request) {
        log.info("Requisicao para alterar senha do usuario ID: {}", id);
        usuarioService.alterarSenha(id, request);
        return ResponseEntity.noContent().build();
    }
}
