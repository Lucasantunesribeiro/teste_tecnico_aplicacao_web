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
    public static final String CSRF_HEADER_SCHEME = "csrfHeader";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Usuarios API")
                .version("1.0.0")
                .description(
                    "Solution TI - Teste Técnico — API de gerenciamento de usuários e endereços. "
                        + "A autenticação da aplicação web é baseada em cookies httpOnly. "
                        + "Em operações mutáveis (POST, PUT, PATCH e DELETE), clientes browser "
                        + "devem enviar também o header X-XSRF-TOKEN com o valor do cookie XSRF-TOKEN."
                ))
            .components(new Components()
                .addSecuritySchemes(COOKIE_AUTH_SCHEME,
                    new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.COOKIE)
                        .name("ACCESS_TOKEN")
                        .description("Cookie de sessão emitido após o login e enviado automaticamente pelo browser."))
                .addSecuritySchemes(CSRF_HEADER_SCHEME,
                    new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER)
                        .name("X-XSRF-TOKEN")
                        .description("Header exigido em requisições mutáveis autenticadas via cookies.")));
    }
}
