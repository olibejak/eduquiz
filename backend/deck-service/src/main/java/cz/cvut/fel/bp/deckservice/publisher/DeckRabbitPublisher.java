package cz.cvut.fel.bp.deckservice.publisher;

import cz.cvut.fel.bp.deckservice.service.event.DeckDeletedEvent;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class DeckRabbitPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final String exchangeName;

    private static final String ROUTING_KEY = "deck.deleted";

    public DeckRabbitPublisher(RabbitTemplate rabbitTemplate,
                               @Value("${app.rabbitmq.exchange.deck}") String exchangeName) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchangeName = exchangeName;
    }

    @CircuitBreaker(name = "rabbitPublisher", fallbackMethod = "fallbackPublish")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishDeckDeleted(DeckDeletedEvent event) {
        log.info("Publishing deck deleted event for deckId: {}", event.deckId());
        rabbitTemplate.convertAndSend(exchangeName, ROUTING_KEY, event);
    }

    private void fallbackPublish(DeckDeletedEvent event, Throwable t) {
        // TODO: implement fallback method
        log.error("RabbitMQ unreachable! Event for deleted deckId: {} not sent.", event.deckId(), t);
    }
}