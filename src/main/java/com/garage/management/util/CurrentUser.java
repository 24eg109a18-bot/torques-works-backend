package com.garage.management.util;

import com.garage.management.entity.Role;
import com.garage.management.exception.UnauthorizedException;
import com.garage.management.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/** Convenience access to the logged-in user (from the JWT) inside the service layer. */
public final class CurrentUser {
    private CurrentUser() {}

    public static CustomUserDetails get() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails details)) {
            throw new UnauthorizedException("You must be logged in");
        }
        return details;
    }

    public static Long id() { return get().getId(); }
    public static Role role() { return get().getRole(); }
}
