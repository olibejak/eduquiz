package cz.cvut.fel.bp.userservice.publisher;

import cz.cvut.fel.bp.userservice.service.event.UserDeletedEvent;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class UserRabbitPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final String exchangeName;

    private static final String ROUTING_KEY = "user.deleted";

    public UserRabbitPublisher(RabbitTemplate rabbitTemplate,
                               @Value("${app.rabbitmq.exchange.user}") String exchangeName) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchangeName = exchangeName;
    }

    @CircuitBreaker(name = "rabbitPublisher", fallbackMethod = "fallbackPublish")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishUserDeleted(UserDeletedEvent event) {
        log.info("Publishing user deleted event for userId: {}", event.userId());
        rabbitTemplate.convertAndSend(exchangeName, ROUTING_KEY, event);
    }

    private void fallbackPublish(UserDeletedEvent event, Throwable t) {
        // TODO: implement fallback method
        log.error("RabbitMQ unreachable! Event for deleted userId: {} not sent.", event.userId(), t);
    }
}