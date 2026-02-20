package org.ashkelyonok.userservice.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class SecurityUtil {

    private final JwtUtil jwtUtil;

    /**
     * Extracts the User ID from the JWT stored in the SecurityContext.
     */
    public Long getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Unauthenticated access attempt");
        }

        Object credentials = authentication.getCredentials();

        if (!(credentials instanceof String token)) {
            throw new AccessDeniedException("Invalid security context: missing token");
        }

        return jwtUtil.extractUserId(token);
    }

    /**
     * Checks if the current user has the ROLE_ADMIN.
     */
    public boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) return false;

        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMIN"));
    }

    /**
     * Enforces that the current user owns the resource, OR is an Admin.
     * @param resourceOwnerId The ID of the user who owns the data.
     */
    public void checkOwnership(Long resourceOwnerId) {
        if (isAdmin()) {
            return;
        }

        Long currentUserId = getAuthenticatedUserId();

        if (!Objects.equals(currentUserId, resourceOwnerId)) {
            throw new AccessDeniedException("Access Denied: You do not own this resource.");
        }
    }
}
