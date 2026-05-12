package cz.cvut.fel.bp.userservice.exception.handler;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalCircuitBreakerExceptionHandler {

    @ExceptionHandler(CallNotPermittedException.class)
    public ResponseEntity<String> handleCircuitBreakerOpen(CallNotPermittedException ex) {
        log.error(ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .header("Retry-After", "15")
                .body("Služba je momentálně přetížená. Zkuste to prosím za chvíli.");
    }
}