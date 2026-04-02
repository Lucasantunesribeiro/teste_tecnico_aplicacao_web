package com.solutionti.usuarios.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String COOKIE_AUTH_SCHEME = "cookieAuth";
    public static final String REFRESH_COOKIE_AUTH_SCHEME = "refreshCookieAuth";
    public static final String ACCESS_TOKEN_COOKIE_NAME = "ACCESS_TOKEN";
    public static final String REFRESH_TOKEN_COOKIE_NAME = "REFRESH_TOKEN";
    public static final String CSRF_HEADER_NAME = "X-XSRF-TOKEN";
    public static final String CSRF_COOKIE_NAME = "XSRF-TOKEN";
    public static final String CSRF_HEADER_DESCRIPTION =
        "Header obrigatorio em requisicoes POST, PUT, PATCH e DELETE. "
            + "Deve repetir o valor do cookie XSRF-TOKEN enviado pelo backend.";
    public static final String REFRESH_COOKIE_DESCRIPTION =
        "Cookie httpOnly de refresh enviado automaticamente pelo browser.";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Usuarios API")
                .version("1.0.0")
                .description(
                    "Solution TI - Teste Tecnico - API de gerenciamento de usuarios e enderecos. "
                        + "A autenticacao da aplicacao web usa cookies httpOnly: ACCESS_TOKEN para a sessao normal "
                        + "e REFRESH_TOKEN para renovar a sessao. Em operacoes mutaveis (POST, PUT, PATCH e DELETE), "
                        + "clientes browser devem enviar tambem o header X-XSRF-TOKEN com o valor do cookie XSRF-TOKEN."
                ))
            .components(new Components()
                .addSecuritySchemes(COOKIE_AUTH_SCHEME,
                    new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.COOKIE)
                        .name(ACCESS_TOKEN_COOKIE_NAME)
                        .description("Cookie de sessao usado para autenticar requisicoes protegidas."))
                .addSecuritySchemes(REFRESH_COOKIE_AUTH_SCHEME,
                    new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.COOKIE)
                        .name(REFRESH_TOKEN_COOKIE_NAME)
                        .description(REFRESH_COOKIE_DESCRIPTION)));
    }
}
