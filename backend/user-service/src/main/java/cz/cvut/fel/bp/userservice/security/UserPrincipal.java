package cz.cvut.fel.bp.userservice.security;

import cz.cvut.fel.bp.userservice.model.UserRole;
import lombok.Builder;

import java.util.UUID;

/**
 * UserPrincipal is a record that represents the authenticated user's principal information.
 * @param id User ID
 * @param username Username of the user, used for display purposes
 * @param role Role of the user (e.g., USER, ADMIN), used for authorization decisions
 */
@Builder
public record UserPrincipal(
        UUID id,
        String username,
        String email,
        String role
) {}
