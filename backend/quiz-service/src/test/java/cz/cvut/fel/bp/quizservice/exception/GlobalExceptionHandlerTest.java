package cz.cvut.fel.bp.quizservice.exception;

import cz.cvut.fel.bp.quizservice.dto.ErrorResponseDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handlesResourceNotFound() {
        MockHttpServletRequest request = request("/sessions/PIN123");

        ErrorResponseDTO response = handler.handleResourceNotFound(new ResourceNotFoundException("missing"), request).getBody();

        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(404);
        assertThat(response.error()).isEqualTo("Not Found");
        assertThat(response.message()).isEqualTo("missing");
        assertThat(response.path()).isEqualTo("/sessions/PIN123");
    }

    @Test
    void handlesInvalidSessionState() {
        MockHttpServletRequest request = request("/sessions/PIN123/join");

        ErrorResponseDTO response = handler.handleInvalidSessionState(new InvalidSessionStateException("wrong state"), request).getBody();

        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(409);
        assertThat(response.message()).isEqualTo("wrong state");
    }

    @Test
    void handlesDuplicateParticipant() {
        MockHttpServletRequest request = request("/sessions/PIN123/join");

        ErrorResponseDTO response = handler.handleDuplicateParticipant(new DuplicateParticipantException("duplicate"), request).getBody();

        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(409);
        assertThat(response.message()).isEqualTo("duplicate");
    }

    @Test
    void handlesAccessDenied() {
        MockHttpServletRequest request = request("/sessions/PIN123/start");

        ErrorResponseDTO response = handler.handleAccessDenied(new org.springframework.security.access.AccessDeniedException("denied"), request).getBody();

        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(403);
        assertThat(response.message()).isEqualTo("denied");
    }

    @Test
    void handlesIllegalArgument() {
        MockHttpServletRequest request = request("/sessions/PIN123/start");

        ErrorResponseDTO response = handler.handleIllegalArgument(new IllegalArgumentException("bad request"), request).getBody();

        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(400);
        assertThat(response.message()).isEqualTo("bad request");
    }

    @Test
    void handlesValidationErrors() throws Exception {
        MockHttpServletRequest request = request("/sessions/PIN123/join");
        MethodParameter parameter = methodParameter();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new DummyRequest(""), "dummyRequest");
        bindingResult.addError(new FieldError("dummyRequest", "nickname", "must not be blank"));
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(parameter, bindingResult);

        ErrorResponseDTO response = handler.handleValidationExceptions(exception, request).getBody();

        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(400);
        assertThat(response.message()).contains("must not be blank");
    }

    @Test
    void handlesMalformedJsonPayload() {
        MockHttpServletRequest request = request("/sessions/PIN123/join");
        HttpMessageNotReadableException exception = new HttpMessageNotReadableException(
                "Malformed JSON",
                new MockHttpInputMessage("{}".getBytes())
        );

        ErrorResponseDTO response = handler.handleHttpMessageNotReadable(exception, request).getBody();

        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(400);
        assertThat(response.message()).isEqualTo("Malformed JSON");
    }

    @Test
    void handlesUnhandledExceptionAsServerError() {
        MockHttpServletRequest request = request("/sessions/PIN123/join");

        ErrorResponseDTO response = handler.handleAllOtherExceptions(new RuntimeException("boom"), request).getBody();

        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(500);
        assertThat(response.error()).isEqualTo("Internal Server Error");
        assertThat(response.message()).isEqualTo("boom");
    }

    private MockHttpServletRequest request(String path) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(path);
        return request;
    }

    private MethodParameter methodParameter() throws NoSuchMethodException {
        Method method = GlobalExceptionHandlerTest.class.getDeclaredMethod("dummyEndpoint", DummyRequest.class);
        return new MethodParameter(method, 0);
    }

    @SuppressWarnings("unused")
    private void dummyEndpoint(@Valid DummyRequest request) {
    }

    private record DummyRequest(@NotBlank String nickname) {
    }
}



