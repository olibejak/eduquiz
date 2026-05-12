package cz.cvut.fel.bp.quizservice.exception.handler;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
@Slf4j
public class QuizWebSocketExceptionHandler {

    @MessageExceptionHandler(CallNotPermittedException.class)
    @SendToUser("/queue/errors")
    public String handleCircuitBreakerOpen(CallNotPermittedException ex) {
        log.warn("WebSocket action rejected - circuit breaker is OPEN");
        return "The service is temporarily overloaded. The action (sending an answer / advancing the game) cannot be performed.";
    }

    @MessageExceptionHandler(Exception.class)
    @SendToUser("/queue/errors")
    public String handleGeneralException(Exception ex) {
        log.error("Unexpected WebSocket error", ex);
        return "An unexpected error occurred while processing the message.";
    }
}
