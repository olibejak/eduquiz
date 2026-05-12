package cz.cvut.fel.bp.flashcardsservice.security;

import lombok.Builder;

import java.util.UUID;

@Builder
public record UserPrincipal(
        UUID id,
        String username,
        String role
) {}

