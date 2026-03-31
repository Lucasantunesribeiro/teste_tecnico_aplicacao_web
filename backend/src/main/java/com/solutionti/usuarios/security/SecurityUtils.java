package com.solutionti.usuarios.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

public class SecurityUtils {

    private SecurityUtils() {
        throw new IllegalStateException("Classe utilitária não deve ser instanciada");
    }

    public static UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        String principalName = authentication.getName();
        try {
            return UUID.fromString(principalName);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) return false;
        return authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch("ROLE_ADMIN"::equals);
    }

    public static boolean isOwner(UUID resourceOwnerId) {
        UUID currentUserId = getCurrentUserId();
        if (currentUserId == null || resourceOwnerId == null) return false;
        return currentUserId.equals(resourceOwnerId);
    }
}
