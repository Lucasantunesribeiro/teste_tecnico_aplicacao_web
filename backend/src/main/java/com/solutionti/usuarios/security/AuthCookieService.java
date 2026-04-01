package com.solutionti.usuarios.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.WebUtils;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class AuthCookieService {

    public static final String ACCESS_COOKIE_NAME = "ACCESS_TOKEN";
    public static final String REFRESH_COOKIE_NAME = "REFRESH_TOKEN";

    @Value("${jwt.access-expiration:${jwt.expiration:900000}}")
    private long accessTokenExpirationMs;

    @Value("${jwt.refresh-expiration:604800000}")
    private long refreshTokenExpirationMs;

    @Value("${security.cookies.secure:false}")
    private boolean secureCookies;

    @Value("${security.cookies.same-site:Lax}")
    private String sameSite;

    @Value("${security.cookies.domain:}")
    private String cookieDomain;

    public void writeSessionCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        addCookie(response, buildAccessCookie(accessToken));
        addCookie(response, buildRefreshCookie(refreshToken));
    }

    public void clearSessionCookies(HttpServletResponse response) {
        addCookie(response, clearCookie(ACCESS_COOKIE_NAME, "/"));
        addCookie(response, clearCookie(REFRESH_COOKIE_NAME, "/api/auth"));
        addCookie(response, clearCookie("XSRF-TOKEN", "/"));
    }

    public String extractRefreshToken(HttpServletRequest request) {
        var cookie = WebUtils.getCookie(request, REFRESH_COOKIE_NAME);
        return cookie != null ? cookie.getValue() : null;
    }

    private ResponseCookie buildAccessCookie(String token) {
        return baseCookie(ACCESS_COOKIE_NAME, "/", accessTokenExpirationMs)
            .value(token)
            .httpOnly(true)
            .build();
    }

    private ResponseCookie buildRefreshCookie(String token) {
        return baseCookie(REFRESH_COOKIE_NAME, "/api/auth", refreshTokenExpirationMs)
            .value(token)
            .httpOnly(true)
            .build();
    }

    private ResponseCookie clearCookie(String name, String path) {
        return baseCookie(name, path, 0)
            .value("")
            .httpOnly(!"XSRF-TOKEN".equals(name))
            .build();
    }

    private ResponseCookie.ResponseCookieBuilder baseCookie(String name, String path, long maxAgeMs) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(name, "")
            .path(path)
            .secure(secureCookies)
            .sameSite(sameSite)
            .maxAge(Math.max(0, maxAgeMs / 1000));

        if (StringUtils.hasText(cookieDomain)) {
            builder.domain(cookieDomain);
        }

        return builder;
    }

    private void addCookie(HttpServletResponse response, ResponseCookie cookie) {
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
