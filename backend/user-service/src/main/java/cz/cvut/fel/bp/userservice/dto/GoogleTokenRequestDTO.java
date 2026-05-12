package cz.cvut.fel.bp.userservice.dto;

import jakarta.validation.constraints.NotBlank;

public record GoogleTokenRequestDTO(
        @NotBlank(message = "Google token cannot be blank")
        String googleToken
) {}
