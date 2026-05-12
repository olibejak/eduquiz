package cz.cvut.fel.bp.deckservice.exception.handler;

import cz.cvut.fel.bp.deckservice.dto.ErrorResponseDTO;
import cz.cvut.fel.bp.deckservice.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

/**
 * Global exception handler for the application.
 * Intercepts exceptions thrown by controllers or services and translates them
 * into standardized {@link ErrorResponseDTO} JSON responses.
 * Also logs the exceptions for monitoring and debugging purposes.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * Handles ResourceNotFoundException when a requested entity does not exist in the database.
     * Logs as a warning since it's a client-side error.
     *
     * @param exception The caught exception.
     * @param request The current HTTP request.
     * @return A response entity containing the error details with a 404 Not Found status.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleResourceNotFound(ResourceNotFoundException exception, HttpServletRequest request) {
        log.warn("Resource not found requestUri={}, message={}", request.getRequestURI(), exception.getMessage());
        return buildErrorResponse(exception, HttpStatus.NOT_FOUND, request);
    }

    /**
     * Handles AccessDeniedException when a user lacks the necessary permissions.
     * Logs as a warning to track potential unauthorized access attempts.
     *
     * @param exception The caught exception.
     * @param request The current HTTP request.
     * @return A response entity containing the error details with a 403 Forbidden status.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> handleAccessDenied(AccessDeniedException exception, HttpServletRequest request) {
        log.warn("Access denied requestUri={}, message={}", request.getRequestURI(), exception.getMessage());
        return buildErrorResponse(exception, HttpStatus.FORBIDDEN, request);
    }

    /**
     * Handles IllegalArgumentException typically thrown for invalid method arguments or validation failures.
     * Logs as a warning.
     *
     * @param exception The caught exception.
     * @param request The current HTTP request.
     * @return A response entity containing the error details with a 400 Bad Request status.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalArgument(IllegalArgumentException exception, HttpServletRequest request) {
        log.warn("Illegal argument requestUri={}, message={}", request.getRequestURI(), exception.getMessage());
        return buildErrorResponse(exception, HttpStatus.BAD_REQUEST, request);
    }

    /**
     * Fallback handler for any uncaught exceptions (e.g., NullPointerException, database down).
     * Logs the full stack trace as an ERROR since this represents a server-side bug or failure.
     *
     * @param exception The caught exception.
     * @param request The current HTTP request.
     * @return A response entity containing generic error details with a 500 Internal Server Error status.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleAllOtherExceptions(Exception exception, HttpServletRequest request) {
        log.error("Unhandled exception requestUri={}", request.getRequestURI(), exception);
        return buildErrorResponse(exception, HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    /**
     * Handles MethodArgumentNotValidException which occurs when @Valid validation on request bodies fails.
     * Logs as a warning since it's a client-side error.
     * @param exception The caught exception containing validation error details.
     * @param request The current HTTP request.
     * @return A response entity containing the error details with a 400 Bad Request status.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationExceptions(MethodArgumentNotValidException exception, HttpServletRequest request) {
        log.warn("Validation failed at {}: {}", request.getRequestURI(), exception.getMessage());
        return buildErrorResponse(exception  , HttpStatus.BAD_REQUEST, request);
    }

    /**
     * Handles HttpMessageNotReadableException which occurs when the request body is malformed (e.g., invalid JSON).
     * @param exception The caught exception.
     * @param request The current HTTP request.
     * @return A response entity containing the error details with a 400 Bad Request status.
     */
    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDTO> handleHttpMessageNotReadable(Exception exception, HttpServletRequest request) {
        log.warn("Malformed JSON payload at {}: {}", request.getRequestURI(), exception.getMessage());
        return buildErrorResponse(exception, HttpStatus.BAD_REQUEST, request);
    }

    /**
     * Helper method to construct the standardized error response.
     *
     * @param exception The exception that was thrown.
     * @param status The HTTP status to be returned to the client.
     * @param request The HTTP request where the error originated.
     * @return A fully populated ResponseEntity wrapping the ErrorResponseDTO.
     */
    private ResponseEntity<ErrorResponseDTO> buildErrorResponse(Exception exception, HttpStatus status, HttpServletRequest request) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                exception.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, status);
    }
}
