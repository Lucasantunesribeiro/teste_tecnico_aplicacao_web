package com.solutionti.usuarios.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solutionti.usuarios.dto.request.AlterarSenhaRequest;
import com.solutionti.usuarios.dto.request.AtualizarUsuarioRequest;
import com.solutionti.usuarios.dto.request.LoginRequest;
import com.solutionti.usuarios.dto.request.UsuarioRequest;
import com.solutionti.usuarios.security.AuthCookieService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests covering RBAC boundaries for /api/usuarios.
 * Validates that USER role cannot access admin-only endpoints and
 * that each role can only operate on data it owns.
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class UsuarioControllerIntegrationTest {

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

    // =========================================================
    // USER role restrictions
    // =========================================================

    @Test
    void usuarioNaoPodeCriarOutroUsuario() throws Exception {
        Cookie userCookie = loginAndExtractCookie("39053344705", "User123!", AuthCookieService.ACCESS_COOKIE_NAME);

        UsuarioRequest novoUsuario = new UsuarioRequest(
            "Novo Fulano", "11144477735", LocalDate.of(1992, 3, 10), "Senha123!"
        );

        mockMvc.perform(post("/api/usuarios")
                .with(csrf())
                .cookie(userCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(novoUsuario)))
            .andExpect(status().isForbidden());
    }

    @Test
    void usuarioNaoPodeListarTodosOsUsuarios() throws Exception {
        Cookie userCookie = loginAndExtractCookie("39053344705", "User123!", AuthCookieService.ACCESS_COOKIE_NAME);

        mockMvc.perform(get("/api/usuarios").cookie(userCookie))
            .andExpect(status().isForbidden());
    }

    @Test
    void usuarioNaoPodeVerDadosDeOutroUsuario() throws Exception {
        Cookie adminCookie = loginAndExtractCookie("52998224725", "Admin123!", AuthCookieService.ACCESS_COOKIE_NAME);
        Cookie userCookie  = loginAndExtractCookie("39053344705", "User123!",  AuthCookieService.ACCESS_COOKIE_NAME);

        // Admin fetches the admin's own ID
        String adminId = extractUserId(adminCookie);

        // USER tries to view the ADMIN's profile
        mockMvc.perform(get("/api/usuarios/{id}", adminId).cookie(userCookie))
            .andExpect(status().isForbidden());
    }

    @Test
    void usuarioPodeVerOsProprioDados() throws Exception {
        Cookie userCookie = loginAndExtractCookie("39053344705", "User123!", AuthCookieService.ACCESS_COOKIE_NAME);
        String userId = extractUserId(userCookie);

        mockMvc.perform(get("/api/usuarios/{id}", userId).cookie(userCookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.cpf").value("39053344705"));
    }

    @Test
    void usuarioNaoPodeDeletarOutroUsuario() throws Exception {
        Cookie adminCookie = loginAndExtractCookie("52998224725", "Admin123!", AuthCookieService.ACCESS_COOKIE_NAME);
        Cookie userCookie  = loginAndExtractCookie("39053344705", "User123!",  AuthCookieService.ACCESS_COOKIE_NAME);

        String adminId = extractUserId(adminCookie);

        mockMvc.perform(delete("/api/usuarios/{id}", adminId)
                .with(csrf())
                .cookie(userCookie))
            .andExpect(status().isForbidden());
    }

    // =========================================================
    // PATCH /senha — password change RBAC
    // =========================================================

    @Test
    void usuarioPodeAlterarAPropriaSenhaComSenhaAtualCorreta() throws Exception {
        Cookie userCookie = loginAndExtractCookie("39053344705", "User123!", AuthCookieService.ACCESS_COOKIE_NAME);
        String userId = extractUserId(userCookie);

        AlterarSenhaRequest req = new AlterarSenhaRequest("User123!", "NovaUser123!");

        mockMvc.perform(patch("/api/usuarios/{id}/senha", userId)
                .with(csrf())
                .cookie(userCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isNoContent());

        // Restore original password so other tests are not affected
        mockMvc.perform(patch("/api/usuarios/{id}/senha", userId)
                .with(csrf())
                .cookie(loginAndExtractCookie("39053344705", "NovaUser123!", AuthCookieService.ACCESS_COOKIE_NAME))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new AlterarSenhaRequest("NovaUser123!", "User123!"))))
            .andExpect(status().isNoContent());
    }

    @Test
    void usuarioNaoPodeAlterarSenhaComSenhaAtualErrada() throws Exception {
        Cookie userCookie = loginAndExtractCookie("39053344705", "User123!", AuthCookieService.ACCESS_COOKIE_NAME);
        String userId = extractUserId(userCookie);

        AlterarSenhaRequest req = new AlterarSenhaRequest("SenhaErrada1!", "NovaUser123!");

        mockMvc.perform(patch("/api/usuarios/{id}/senha", userId)
                .with(csrf())
                .cookie(userCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Senha atual incorreta"));
    }

    @Test
    void usuarioNaoPodeAlterarSenhaDeOutroUsuario() throws Exception {
        Cookie adminCookie = loginAndExtractCookie("52998224725", "Admin123!", AuthCookieService.ACCESS_COOKIE_NAME);
        Cookie userCookie  = loginAndExtractCookie("39053344705", "User123!",  AuthCookieService.ACCESS_COOKIE_NAME);

        String adminId = extractUserId(adminCookie);

        AlterarSenhaRequest req = new AlterarSenhaRequest("Admin123!", "Hacked123!");

        mockMvc.perform(patch("/api/usuarios/{id}/senha", adminId)
                .with(csrf())
                .cookie(userCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isForbidden());
    }

    @Test
    void adminPodeRedefinirSenhaDeQualquerUsuarioSemSenhaAtual() throws Exception {
        Cookie adminCookie = loginAndExtractCookie("52998224725", "Admin123!", AuthCookieService.ACCESS_COOKIE_NAME);
        Cookie userCookie  = loginAndExtractCookie("39053344705", "User123!",  AuthCookieService.ACCESS_COOKIE_NAME);
        String userId = extractUserId(userCookie);

        AlterarSenhaRequest req = new AlterarSenhaRequest(null, "Redefinida1!");

        mockMvc.perform(patch("/api/usuarios/{id}/senha", userId)
                .with(csrf())
                .cookie(adminCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isNoContent());

        // Restore original password
        mockMvc.perform(patch("/api/usuarios/{id}/senha", userId)
                .with(csrf())
                .cookie(adminCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new AlterarSenhaRequest(null, "User123!"))))
            .andExpect(status().isNoContent());
    }

    // =========================================================
    // ADMIN happy-path: create → list → update (no password) → delete
    // =========================================================

    @Test
    void adminPodeCriarListarAtualizarEDeletarUsuario() throws Exception {
        Cookie adminCookie = loginAndExtractCookie("52998224725", "Admin123!", AuthCookieService.ACCESS_COOKIE_NAME);

        // Create
        UsuarioRequest criarReq = new UsuarioRequest(
            "Temp User", "11144477735", LocalDate.of(1988, 7, 22), "TempSenha1!"
        );
        MvcResult criarResult = mockMvc.perform(post("/api/usuarios")
                .with(csrf())
                .cookie(adminCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(criarReq)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.cpf").value("11144477735"))
            .andReturn();

        String novoId = objectMapper.readTree(criarResult.getResponse().getContentAsString())
            .get("id").asText();

        // List — newly created user appears
        mockMvc.perform(get("/api/usuarios").cookie(adminCookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").isNumber());

        // Update without changing the password
        AtualizarUsuarioRequest atualizarReq = new AtualizarUsuarioRequest(
            "Temp User Atualizado", "11144477735", LocalDate.of(1988, 7, 22), null
        );
        mockMvc.perform(put("/api/usuarios/{id}", novoId)
                .with(csrf())
                .cookie(adminCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(atualizarReq)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nome").value("Temp User Atualizado"));

        // Delete
        mockMvc.perform(delete("/api/usuarios/{id}", novoId)
                .with(csrf())
                .cookie(adminCookie))
            .andExpect(status().isNoContent());
    }

    // =========================================================
    // Helpers
    // =========================================================

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

    /** Extracts the userId from the /api/auth/me endpoint. */
    private String extractUserId(Cookie accessCookie) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/auth/me").cookie(accessCookie))
            .andExpect(status().isOk())
            .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString())
            .get("userId").asText();
    }
}
