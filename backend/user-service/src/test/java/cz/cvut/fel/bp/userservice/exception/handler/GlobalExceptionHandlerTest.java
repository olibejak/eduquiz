package cz.cvut.fel.bp.userservice.exception.handler;

import cz.cvut.fel.bp.userservice.dto.ErrorResponseDTO;
import cz.cvut.fel.bp.userservice.exception.DuplicateResourceException;
import cz.cvut.fel.bp.userservice.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();

    @Test
    void shouldReturnSafeInternalServerErrorForUnhandledException() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/test/internalerror");

        ErrorResponseDTO response = globalExceptionHandler.handleUnexpectedException(
                new RuntimeException("sensitive internal detail"),
                request
        );

        assertNotNull(response);
        assertNotNull(response.timestamp());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.status());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), response.error());
        assertEquals("Internal server error.", response.message());
        assertEquals("/test/internalerror", response.path());
    }

    @Test
    void shouldReturnNotFoundForResourceNotFoundException() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/user/99");

        ErrorResponseDTO response = globalExceptionHandler.handleResourceNotFound(
                new ResourceNotFoundException("User not found"),
                request
        );

        assertNotNull(response.timestamp());
        assertEquals(HttpStatus.NOT_FOUND.value(), response.status());
        assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), response.error());
        assertEquals("User not found", response.message());
        assertEquals("/user/99", response.path());
    }

    @Test
    void shouldReturnConflictForDuplicateResourceException() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/user/register/gmail");

        ErrorResponseDTO response = globalExceptionHandler.handleDuplicateResource(
                new DuplicateResourceException("Username already exists"),
                request
        );

        assertNotNull(response.timestamp());
        assertEquals(HttpStatus.CONFLICT.value(), response.status());
        assertEquals(HttpStatus.CONFLICT.getReasonPhrase(), response.error());
        assertEquals("Username already exists", response.message());
        assertEquals("/user/register/gmail", response.path());
    }
}

