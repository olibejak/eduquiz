package cz.cvut.fel.bp.userservice.dto;

import lombok.Builder;

import java.time.LocalDateTime;

/**
 * DTO for sending error messages to the client.
 * Primarily used for invalid registration or login.
 * @param timestamp timestamp of the error occurrence
 * @param status HTTP status code
 * @param error HTTP status phrase
 * @param message Custom message of the error
 */
@Builder
public record ErrorResponseDTO(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path
) {}
