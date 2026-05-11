package cz.cvut.fel.bp.flashcardsservice.exception.handler;

import cz.cvut.fel.bp.flashcardsservice.exception.*;
import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        when(request.getRequestURI()).thenReturn("/flashcards/1");
        when(request.getMethod()).thenReturn("GET");
    }

    @Test
    void testHandleDeckNotFound_Returns404() {
        var exception = new DeckNotFoundException(1L);

        var response = handler.handleDeckNotFound(exception, request);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND.value(), response.status());
        assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), response.error());
        assertTrue(response.message().contains("not found"));
    }

    @Test
    void testHandleInvalidFlashcardRating_Returns400() {
        var exception = new InvalidFlashcardRatingException("Invalid rating: UNKNOWN");

        var response = handler.handleInvalidFlashcardRating(exception, request);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.status());
        assertEquals(HttpStatus.BAD_REQUEST.getReasonPhrase(), response.error());
        assertTrue(response.message().contains("Invalid"));
    }

    @Test
    void testHandleStudySessionException_Returns409() {
        var exception = new StudySessionException("No questions available");

        var response = handler.handleStudySessionException(exception, request);

        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT.value(), response.status());
        assertEquals(HttpStatus.CONFLICT.getReasonPhrase(), response.error());
        assertTrue(response.message().contains("No questions"));
    }

    @Test
    void testHandleFlashcardReviewException_Returns400() {
        var exception = new FlashcardReviewException("Review processing failed");

        var response = handler.handleFlashcardReviewException(exception, request);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.status());
        assertEquals(HttpStatus.BAD_REQUEST.getReasonPhrase(), response.error());
    }

    @Test
    void testHandleFlashcardException_Returns400() {
        var exception = new FlashcardException("Domain error");

        var response = handler.handleFlashcardException(exception, request);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.status());
        assertEquals(HttpStatus.BAD_REQUEST.getReasonPhrase(), response.error());
    }

    @Test
    void testHandleFeignException_Returns503() {
        var exception = mock(FeignException.class);
        when(exception.status()).thenReturn(503);
        when(exception.getMessage()).thenReturn("Deck service down");

        var response = handler.handleFeignException(exception, request);

        assertNotNull(response);
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE.value(), response.status());
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase(), response.error());
        assertTrue(response.message().contains("temporarily unavailable"));
    }

    @Test
    void testHandleIllegalArgument_Returns400() {
        var exception = new IllegalArgumentException("Invalid session size");

        var response = handler.handleIllegalArgument(exception, request);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.status());
        assertEquals(HttpStatus.BAD_REQUEST.getReasonPhrase(), response.error());
    }

    @Test
    void testHandleUnexpectedException_Returns500() {
        var exception = new RuntimeException("Unexpected error");

        var response = handler.handleUnexpectedException(exception, request);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.status());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), response.error());
        assertTrue(response.message().contains("Internal server error"));
    }

    @Test
    void testErrorResponse_IncludesRequestPath() {
        var exception = new DeckNotFoundException(1L);

        var response = handler.handleDeckNotFound(exception, request);

        assertEquals("/flashcards/1", response.path());
    }

    @Test
    void testErrorResponse_IncludesTimestamp() {
        var exception = new DeckNotFoundException(1L);

        var response = handler.handleDeckNotFound(exception, request);

        assertNotNull(response.timestamp());
    }

    @Test
    void testErrorResponse_HasAllRequiredFields() {
        var exception = new InvalidFlashcardRatingException("Invalid");

        var response = handler.handleInvalidFlashcardRating(exception, request);

        assertNotNull(response.timestamp());
        assertNotNull(response.status());
        assertNotNull(response.error());
        assertNotNull(response.message());
        assertNotNull(response.path());
    }

    @Test
    void testHandleMultipleDifferentExceptions_EachReturnCorrectStatus() {

        var deckNotFound = new DeckNotFoundException(1L);
        var invalidRating = new InvalidFlashcardRatingException("Bad");
        var sessionError = new StudySessionException("No session");

        var resp1 = handler.handleDeckNotFound(deckNotFound, request);
        var resp2 = handler.handleInvalidFlashcardRating(invalidRating, request);
        var resp3 = handler.handleStudySessionException(sessionError, request);

        assertEquals(404, resp1.status());
        assertEquals(400, resp2.status());
        assertEquals(409, resp3.status());
    }
}

