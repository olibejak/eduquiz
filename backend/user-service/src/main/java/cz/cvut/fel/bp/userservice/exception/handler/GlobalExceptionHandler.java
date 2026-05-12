package cz.cvut.fel.bp.userservice.exception.handler;

import cz.cvut.fel.bp.userservice.dto.ErrorResponseDTO;
import cz.cvut.fel.bp.userservice.exception.DuplicateResourceException;
import cz.cvut.fel.bp.userservice.exception.InvalidCredentialsException;
import cz.cvut.fel.bp.userservice.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;

/**
 * Global exception handler for the application.
 * Intercepts specific exceptions thrown by controllers and translates them
 * into standardized, client-friendly HTTP responses.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handles ResourceNotFoundException when a requested resource is not found in the database.
     *
     * @param exception the thrown ResourceNotFoundException
     * @param request   the HTTP request that resulted in the exception
     * @return a standardized ErrorResponseDTO with a 404 NOT FOUND status
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponseDTO handleResourceNotFound(ResourceNotFoundException exception, HttpServletRequest request) {
        logHandledException(HttpStatus.NOT_FOUND, exception, request);
        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request
        );
    }

    /**
     * Handles AccessDeniedException when a user attempts to access a resource without proper privileges.
     *
     * @param exception the thrown AccessDeniedException
     * @param request   the HTTP request that resulted in the exception
     * @return a standardized ErrorResponseDTO with a 403 FORBIDDEN status
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponseDTO handleAccessDenied(AccessDeniedException exception, HttpServletRequest request) {
        logHandledException(HttpStatus.FORBIDDEN, exception, request);
        return buildErrorResponse(
                HttpStatus.FORBIDDEN,
                "Access denied: " + exception.getMessage(),
                request
        );
    }

    /**
     * Handles IllegalArgumentException when invalid method arguments or inputs are provided.
     *
     * @param exception the thrown IllegalArgumentException
     * @param request   the HTTP request that resulted in the exception
     * @return a standardized ErrorResponseDTO with a 400 BAD REQUEST status
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDTO handleIllegalArgument(IllegalArgumentException exception, HttpServletRequest request) {
        logHandledException(HttpStatus.BAD_REQUEST, exception, request);
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                request
        );
    }

    /**
     * Handles DuplicateResourceException when an attempt is made to create a resource that already exists.
     *
     * @param exception the thrown DuplicateResourceException
     * @param request   the HTTP request that resulted in the exception
     * @return a standardized ErrorResponseDTO with a 409 CONFLICT status
     */
    @ExceptionHandler(DuplicateResourceException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponseDTO handleDuplicateResource(DuplicateResourceException exception, HttpServletRequest request) {
        logHandledException(HttpStatus.CONFLICT, exception, request);
        return buildErrorResponse(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                request
        );
    }

    /**
     * Handles InvalidCredentialsException when authentication fails due to incorrect credentials.
     *
     * @param exception the thrown InvalidCredentialsException
     * @param request   the HTTP request that resulted in the exception
     * @return a standardized ErrorResponseDTO with a 401 UNAUTHORIZED status
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponseDTO handleInvalidCredentials(InvalidCredentialsException exception, HttpServletRequest request) {
        logHandledException(HttpStatus.UNAUTHORIZED, exception, request);
        return buildErrorResponse(
                HttpStatus.UNAUTHORIZED,
                exception.getMessage(),
                request
        );
    }

    /**
     * Handles any unexpected exception that is not covered by the more specific handlers.
     *
     * @param exception the thrown unexpected exception
     * @param request   the HTTP request that resulted in the exception
     * @return a standardized ErrorResponseDTO with a 500 INTERNAL SERVER ERROR status
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponseDTO handleUnexpectedException(Exception exception, HttpServletRequest request) {
        log.error(
                "Unhandled exception status={} type={} path={} method={}",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                exception.getClass().getSimpleName(),
                request.getRequestURI(),
                request.getMethod(),
                exception
        );
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal server error.",
                request
        );
    }

    /**
     * Helper method to construct the standardized ErrorResponseDTO using a builder pattern.
     *
     * @param status  the HTTP status to be returned
     * @param message the detailed error message
     * @param request the HTTP request where the error occurred
     * @return fully populated ErrorResponseDTO
     */
    private ErrorResponseDTO buildErrorResponse(HttpStatus status, String message, HttpServletRequest request) {
        return ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .build();
    }

    private void logHandledException(HttpStatus status, Exception exception, HttpServletRequest request) {
        log.warn(
                "Handled exception status={} type={} path={} method={}",
                status.value(),
                exception.getClass().getSimpleName(),
                request.getRequestURI(),
                request.getMethod()
        );
    }
}