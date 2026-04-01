package com.solutionti.usuarios.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solutionti.usuarios.dto.request.LoginRequest;
import com.solutionti.usuarios.security.AuthCookieService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class EnderecoControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
        .withDatabaseName("usuarios_test")
        .withUsername("test")
        .withPassword("test")
        .withInitScript("db/migration/V1__init.sql");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
        .withExposedPorts(6379);

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
        registry.add("jwt.secret", () -> "dGVzdC1zZWNyZXQtZm9yLWludGVncmF0aW9uLXRlc3RzLXdpdGgtbG9uZy1lbm91Z2gtc2VjcmV0LWtleQ==");
        registry.add("jwt.access-expiration", () -> 900000L);
        registry.add("jwt.refresh-expiration", () -> 604800000L);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void deveListarEnderecosPaginadosParaAdmin() throws Exception {
        Cookie accessCookie = loginAndExtractCookie("52998224725", "Admin123!", AuthCookieService.ACCESS_COOKIE_NAME);

        mockMvc.perform(get("/api/enderecos")
                .cookie(accessCookie)
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(header().exists("X-Request-Id"))
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.content[0].usuarioNome").value("Administrador do Sistema"))
            .andExpect(jsonPath("$.content[0].usuarioCpf").value("52998224725"))
            .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void deveFiltrarEnderecosPorCidadeParaAdmin() throws Exception {
        Cookie accessCookie = loginAndExtractCookie("52998224725", "Admin123!", AuthCookieService.ACCESS_COOKIE_NAME);

        mockMvc.perform(get("/api/enderecos")
                .cookie(accessCookie)
                .param("cidade", "Rio de Janeiro"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].cidade").value("Rio de Janeiro"));
    }

    @Test
    void deveNegarListagemGlobalParaUsuarioComum() throws Exception {
        Cookie accessCookie = loginAndExtractCookie("39053344705", "User123!", AuthCookieService.ACCESS_COOKIE_NAME);

        mockMvc.perform(get("/api/enderecos").cookie(accessCookie))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message").value("Acesso negado: apenas administradores podem listar todos os enderecos"));
    }

    @Test
    void deveAceitarAuthorizationBearerComoCompatibilidadeTransitoria() throws Exception {
        Cookie accessCookie = loginAndExtractCookie("52998224725", "Admin123!", AuthCookieService.ACCESS_COOKIE_NAME);

        mockMvc.perform(get("/api/enderecos")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessCookie.getValue()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(2));
    }

    private Cookie loginAndExtractCookie(String cpf, String senha, String cookieName) throws Exception {
        LoginRequest request = new LoginRequest(cpf, senha);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn();

        Cookie cookie = result.getResponse().getCookie(cookieName);
        assertThat(cookie).isNotNull();
        return cookie;
    }
}
