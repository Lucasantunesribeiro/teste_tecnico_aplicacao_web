package com.solutionti.usuarios.controller;

import com.solutionti.usuarios.config.OpenApiConfig;
import com.solutionti.usuarios.dto.request.LoginRequest;
import com.solutionti.usuarios.dto.response.ErrorResponse;
import com.solutionti.usuarios.dto.response.LoginResponse;
import com.solutionti.usuarios.security.AuthCookieService;
import com.solutionti.usuarios.security.LoginRateLimiter;
import com.solutionti.usuarios.service.AuthService;
import com.solutionti.usuarios.service.auth.AuthSessionResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Autenticacao", description = "Endpoints de autenticacao e sessao")
public class AuthController {

    private final AuthService authService;
    private final LoginRateLimiter rateLimiter;
    private final AuthCookieService authCookieService;

    @Value("${security.trust-forward-headers:false}")
    private boolean trustForwardHeaders;

    @PostMapping("/login")
    @Operation(summary = "Autenticar usuario", description = "Realiza autenticacao via CPF e senha e seta cookies de sessao")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Login realizado com sucesso",
            content = @Content(schema = @Schema(implementation = LoginResponse.class))),
        @ApiResponse(responseCode = "401", description = "Credenciais invalidas",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "422", description = "Dados de entrada invalidos",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "429", description = "Muitas tentativas. Aguarde antes de tentar novamente.",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request,
                                               HttpServletRequest httpRequest,
                                               HttpServletResponse response,
                                               CsrfToken csrfToken) {
        String clientIp = resolveClientIp(httpRequest);
        log.info("Requisicao de login recebida (IP: {})", clientIp);

        rateLimiter.checkRateLimit(clientIp);
        AuthSessionResult session = authService.login(request);
        rateLimiter.resetAttempts(clientIp);

        csrfToken.getToken();
        authCookieService.writeSessionCookies(response, session.accessToken(), session.refreshToken());

        return ResponseEntity.ok()
            .cacheControl(CacheControl.noStore())
            .body(session.response());
    }

    @PostMapping("/refresh")
    @Operation(
        summary = "Renovar sessao",
        description = "Rotaciona o refresh token e emite um novo access token",
        parameters = {
            @Parameter(
                name = OpenApiConfig.CSRF_HEADER_NAME,
                in = ParameterIn.HEADER,
                required = true,
                description = OpenApiConfig.CSRF_HEADER_DESCRIPTION
            )
        }
    )
    @SecurityRequirement(name = "refreshCookieAuth")
    public ResponseEntity<LoginResponse> refresh(
            @CookieValue(name = AuthCookieService.REFRESH_COOKIE_NAME, required = false) String refreshToken,
            HttpServletResponse response,
            CsrfToken csrfToken) {
        AuthSessionResult session = authService.refresh(refreshToken);
        csrfToken.getToken();
        authCookieService.writeSessionCookies(response, session.accessToken(), session.refreshToken());

        return ResponseEntity.ok()
            .cacheControl(CacheControl.noStore())
            .body(session.response());
    }

    @PostMapping("/logout")
    @Operation(
        summary = "Encerrar sessao",
        description = "Revoga o refresh token quando presente e limpa os cookies da sessao no browser.",
        parameters = {
            @Parameter(
                name = OpenApiConfig.REFRESH_TOKEN_COOKIE_NAME,
                in = ParameterIn.COOKIE,
                required = false,
                description = OpenApiConfig.REFRESH_COOKIE_DESCRIPTION
            ),
            @Parameter(
                name = OpenApiConfig.CSRF_HEADER_NAME,
                in = ParameterIn.HEADER,
                required = true,
                description = OpenApiConfig.CSRF_HEADER_DESCRIPTION
            )
        }
    )
    public ResponseEntity<Void> logout(
            @CookieValue(name = AuthCookieService.REFRESH_COOKIE_NAME, required = false) String refreshToken,
            HttpServletResponse response) {
        authService.logout(refreshToken);
        authCookieService.clearSessionCookies(response);
        return ResponseEntity.noContent()
            .cacheControl(CacheControl.noStore())
            .build();
    }

    @GetMapping("/me")
    @Operation(summary = "Sessao atual", description = "Retorna os dados do usuario autenticado")
    @SecurityRequirement(name = "cookieAuth")
    public ResponseEntity<LoginResponse> me(CsrfToken csrfToken) {
        csrfToken.getToken();
        return ResponseEntity.ok()
            .cacheControl(CacheControl.noStore())
            .body(authService.me());
    }

    private String resolveClientIp(HttpServletRequest request) {
        if (!trustForwardHeaders) {
            return request.getRemoteAddr();
        }

        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }
}
