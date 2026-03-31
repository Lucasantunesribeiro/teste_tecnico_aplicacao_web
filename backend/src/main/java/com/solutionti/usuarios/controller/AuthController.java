package com.solutionti.usuarios.controller;

import com.solutionti.usuarios.dto.request.LoginRequest;
import com.solutionti.usuarios.dto.response.ErrorResponse;
import com.solutionti.usuarios.dto.response.LoginResponse;
import com.solutionti.usuarios.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Autenticação", description = "Endpoints de autenticação")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Autenticar usuário", description = "Realiza autenticação via CPF e senha, retornando um token JWT")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Login realizado com sucesso",
            content = @Content(schema = @Schema(implementation = LoginResponse.class))),
        @ApiResponse(responseCode = "400", description = "Credenciais inválidas ou dados incorretos",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "422", description = "Dados de entrada inválidos",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        log.info("Requisição de login recebida para CPF: {}", request.cpf());
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
