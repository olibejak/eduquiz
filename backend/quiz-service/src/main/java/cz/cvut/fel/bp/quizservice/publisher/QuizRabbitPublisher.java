package cz.cvut.fel.bp.quizservice.publisher;

import cz.cvut.fel.bp.quizservice.service.event.QuizEndedEvent;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class QuizRabbitPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final String exchangeName;

    private static final String ROUTING_KEY = "quiz.ended";

    public QuizRabbitPublisher(RabbitTemplate rabbitTemplate,
                               @Value("${app.rabbitmq.exchange.name}") String exchangeName) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchangeName = exchangeName;
    }


    @CircuitBreaker(name = "rabbitPublisher", fallbackMethod = "fallbackPublish")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishQuizEnded(QuizEndedEvent event) {
        log.info("Publishing quiz ended event for lobby: {}", event.lobbyPin());
        rabbitTemplate.convertAndSend(exchangeName, ROUTING_KEY, event);
    }

    private void fallbackPublish(QuizEndedEvent event, Throwable t) {
        // Todo: temporary event storage for retrying later, e.g. in DB or in-memory queue
        log.error("RabbitMQ unreachable! Event for lobby with pin: {} not sent.", event.lobbyPin());
    }
}