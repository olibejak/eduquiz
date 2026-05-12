package cz.cvut.fel.bp.flashcardsservice.exception.handler;

import cz.cvut.fel.bp.flashcardsservice.dto.ErrorResponseDTO;
import cz.cvut.fel.bp.flashcardsservice.exception.*;
import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

/**
 * Global exception handler for the flashcards service.
 * Intercepts flashcard-specific exceptions and external service failures,
 * translating them into standardized, client-friendly HTTP responses.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handles DeckNotFoundException when a deck cannot be found or accessed.
     *
     * @param exception the thrown DeckNotFoundException
     * @param request   the HTTP request that resulted in the exception
     * @return a standardized ErrorResponseDTO with a 404 NOT FOUND status
     */
    @ExceptionHandler(DeckNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponseDTO handleDeckNotFound(DeckNotFoundException exception, HttpServletRequest request) {
        log.warn("Deck not found path={}, method={}, message={}",
                request.getRequestURI(), request.getMethod(), exception.getMessage());
        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request
        );
    }

    /**
     * Handles InvalidFlashcardRatingException when an invalid rating is provided.
     *
     * @param exception the thrown InvalidFlashcardRatingException
     * @param request   the HTTP request that resulted in the exception
     * @return a standardized ErrorResponseDTO with a 400 BAD REQUEST status
     */
    @ExceptionHandler(InvalidFlashcardRatingException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDTO handleInvalidFlashcardRating(InvalidFlashcardRatingException exception, HttpServletRequest request) {
        log.warn("Invalid flashcard rating provided path={}, method={}, message={}",
                request.getRequestURI(), request.getMethod(), exception.getMessage());
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                request
        );
    }

    /**
     * Handles StudySessionException when study session generation fails.
     *
     * @param exception the thrown StudySessionException
     * @param request   the HTTP request that resulted in the exception
     * @return a standardized ErrorResponseDTO with a 409 CONFLICT status
     */
    @ExceptionHandler(StudySessionException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponseDTO handleStudySessionException(StudySessionException exception, HttpServletRequest request) {
        log.warn("Study session generation failed path={}, method={}, message={}",
                request.getRequestURI(), request.getMethod(), exception.getMessage());
        return buildErrorResponse(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                request
        );
    }

    /**
     * Handles FlashcardReviewException when flashcard review processing fails.
     *
     * @param exception the thrown FlashcardReviewException
     * @param request   the HTTP request that resulted in the exception
     * @return a standardized ErrorResponseDTO with a 400 BAD REQUEST status
     */
    @ExceptionHandler(FlashcardReviewException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDTO handleFlashcardReviewException(FlashcardReviewException exception, HttpServletRequest request) {
        log.warn("Flashcard review submission failed path={}, method={}, message={}",
                request.getRequestURI(), request.getMethod(), exception.getMessage());
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                request
        );
    }

    /**
     * Handles generic FlashcardException for other domain-specific errors.
     *
     * @param exception the thrown FlashcardException
     * @param request   the HTTP request that resulted in the exception
     * @return a standardized ErrorResponseDTO with a 400 BAD REQUEST status
     */
    @ExceptionHandler(FlashcardException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDTO handleFlashcardException(FlashcardException exception, HttpServletRequest request) {
        log.warn("Flashcard domain error path={}, method={}, message={}",
                request.getRequestURI(), request.getMethod(), exception.getMessage());
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                request
        );
    }

    /**
     * Handles FeignException when the deck service or other external services are unavailable.
     *
     * @param exception the thrown FeignException
     * @param request   the HTTP request that resulted in the exception
     * @return a standardized ErrorResponseDTO with a 503 SERVICE UNAVAILABLE status
     */
    @ExceptionHandler(FeignException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ErrorResponseDTO handleFeignException(FeignException exception, HttpServletRequest request) {
        log.error("External service unavailable status={}, path={}, method={}, reason={}",
                exception.status(),
                request.getRequestURI(),
                request.getMethod(),
                exception.getMessage());
        return buildErrorResponse(
                HttpStatus.SERVICE_UNAVAILABLE,
                "External service temporarily unavailable. Please try again later.",
                request
        );
    }

    /**
     * Handles IllegalArgumentException when invalid arguments are provided.
     *
     * @param exception the thrown IllegalArgumentException
     * @param request   the HTTP request that resulted in the exception
     * @return a standardized ErrorResponseDTO with a 400 BAD REQUEST status
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDTO handleIllegalArgument(IllegalArgumentException exception, HttpServletRequest request) {
        log.warn("Invalid argument path={}, method={}, message={}",
                request.getRequestURI(), request.getMethod(), exception.getMessage());
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
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
        log.error("Unhandled exception path={}, method={}, type={}, message={}",
                request.getRequestURI(),
                request.getMethod(),
                exception.getClass().getSimpleName(),
                exception.getMessage(),
                exception);
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal server error. Please contact support if the problem persists.",
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
}
