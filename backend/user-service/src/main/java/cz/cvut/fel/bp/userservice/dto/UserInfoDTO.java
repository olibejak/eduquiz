package cz.cvut.fel.bp.userservice.dto;

import lombok.Builder;

import java.util.UUID;

/**
 * DTO containing basic information about the user.
 * Used within microservices.
 * @param id
 * @param username
 * @param role
 */
@Builder
public record UserInfoDTO(
        UUID id,
        String username,
        String role
) {}
