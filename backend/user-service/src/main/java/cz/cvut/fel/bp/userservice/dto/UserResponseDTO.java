package cz.cvut.fel.bp.userservice.dto;

import cz.cvut.fel.bp.userservice.model.UserRole;
import lombok.Builder;

import java.util.UUID;

/**
 * User response DTO used for sending user information back to the client.
 * @param id User ID
 * @param username Username of the user
 * @param email Email of the user
 * @param role Role of the user (e.g., USER, ADMIN)
 */
@Builder
public record UserResponseDTO(
        UUID id,
        String username,
        String email,
        UserRole role
) {}
