package cz.cvut.fel.bp.flashcardsservice.dto;

import lombok.Builder;

import java.time.LocalDateTime;

/**
 * Data Transfer Object representing an API error response.
 * This structured response is returned to the client whenever an exception occurs,
 * providing a consistent and readable error format.
 *
 * @param timestamp The exact time the error occurred.
 * @param status    The HTTP status code (e.g., 404, 500).
 * @param error     The HTTP status error name (e.g., "Not Found").
 * @param message   A human-readable message providing details about the error.
 * @param path      The URI path that was requested when the error occurred.
 */
@Builder
public record ErrorResponseDTO (
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path
) {}
