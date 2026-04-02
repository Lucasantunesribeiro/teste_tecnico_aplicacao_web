package com.solutionti.usuarios.controller;

import com.solutionti.usuarios.dto.response.CepResponse;
import com.solutionti.usuarios.dto.response.ErrorResponse;
import com.solutionti.usuarios.service.CepService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cep")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "CEP", description = "Consulta de endereço por CEP via ViaCEP")
@SecurityRequirement(name = "cookieAuth")
public class CepController {

    private final CepService cepService;

    @GetMapping("/{cep}")
    @Operation(summary = "Consultar CEP", description = "Consulta dados de endereço a partir de um CEP (8 dígitos)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "CEP encontrado",
            content = @Content(schema = @Schema(implementation = CepResponse.class))),
        @ApiResponse(responseCode = "400", description = "CEP inválido ou não encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<CepResponse> consultarCep(@PathVariable String cep) {
        log.debug("Requisição para consultar CEP: {}", cep);
        CepResponse response = cepService.consultarCep(cep);
        return ResponseEntity.ok(response);
    }
}
