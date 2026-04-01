package com.solutionti.usuarios.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solutionti.usuarios.dto.response.ErrorResponse;
import com.solutionti.usuarios.security.CsrfCookieFilter;
import com.solutionti.usuarios.security.JwtAuthenticationFilter;
import com.solutionti.usuarios.security.RequestIdFilter;
import com.solutionti.usuarios.security.SpaCsrfTokenRequestHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RequestIdFilter requestIdFilter;
    private final CsrfCookieFilter csrfCookieFilter;
    private final ObjectMapper objectMapper;

    @Value("${cors.allowed-origins:http://localhost:5173,http://localhost}")
    private String allowedOriginsRaw;

    @Bean
    @Profile("!prod")
    public SecurityFilterChain securityFilterChainDev(HttpSecurity http) throws Exception {
        return configure(http, true);
    }

    @Bean
    @Profile("prod")
    public SecurityFilterChain securityFilterChainProd(HttpSecurity http) throws Exception {
        return configure(http, false);
    }

    private SecurityFilterChain configure(HttpSecurity http, boolean exposeDocs) throws Exception {
        http
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler())
                .ignoringRequestMatchers("/api/auth/login", "/actuator/health", "/swagger-ui/**", "/v3/api-docs/**")
            )
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response, authException) -> {
                    log.warn("Nao autenticado para {} {}", request.getMethod(), request.getRequestURI());
                    writeErrorResponse(
                        response,
                        HttpStatus.UNAUTHORIZED,
                        "Unauthorized",
                        "Nao autenticado",
                        request.getRequestURI(),
                        String.valueOf(request.getAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE))
                    );
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    boolean hasXsrfHeader = StringUtils.hasText(request.getHeader("X-XSRF-TOKEN"));
                    boolean hasXsrfCookie = request.getCookies() != null
                        && Arrays.stream(request.getCookies()).anyMatch(cookie -> "XSRF-TOKEN".equals(cookie.getName()));
                    log.warn(
                        "Acesso negado para {} {} (xsrfHeaderPresent={}, xsrfCookiePresent={})",
                        request.getMethod(),
                        request.getRequestURI(),
                        hasXsrfHeader,
                        hasXsrfCookie
                    );
                    writeErrorResponse(
                        response,
                        HttpStatus.FORBIDDEN,
                        "Forbidden",
                        "Acesso negado",
                        request.getRequestURI(),
                        String.valueOf(request.getAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE))
                    );
                })
            )
            .authorizeHttpRequests(auth -> {
                auth.requestMatchers("/api/auth/login", "/api/auth/refresh", "/api/auth/logout").permitAll();
                auth.requestMatchers("/actuator/health").permitAll();
                if (exposeDocs) {
                    auth.requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll();
                }
                // HTTP-level ADMIN-only rules — run at filter time, before @Valid argument resolution,
                // guaranteeing 403 (not 400) for unauthorized callers regardless of body validity.
                auth.requestMatchers(HttpMethod.POST,   "/api/usuarios").hasRole("ADMIN");
                auth.requestMatchers(HttpMethod.GET,    "/api/usuarios").hasRole("ADMIN");
                auth.requestMatchers(HttpMethod.PUT,    "/api/usuarios/**").hasRole("ADMIN");
                auth.requestMatchers(HttpMethod.DELETE, "/api/usuarios/**").hasRole("ADMIN");
                auth.anyRequest().authenticated();
            })
            .addFilterBefore(requestIdFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(csrfCookieFilter, CsrfFilter.class)
            .addFilterAfter(jwtAuthenticationFilter, CsrfFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        List<String> allowedOrigins = Arrays.stream(allowedOriginsRaw.split(","))
            .map(String::trim)
            .filter(origin -> !origin.isBlank())
            .toList();

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Content-Type", "X-XSRF-TOKEN", "X-Request-Id", "Authorization"));
        configuration.setExposedHeaders(List.of("X-Request-Id"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    private void writeErrorResponse(jakarta.servlet.http.HttpServletResponse response,
                                    HttpStatus status,
                                    String error,
                                    String message,
                                    String path,
                                    String requestId) throws java.io.IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(new ErrorResponse(
            LocalDateTime.now(),
            status.value(),
            error,
            message,
            path,
            requestId,
            null
        )));
    }
}
