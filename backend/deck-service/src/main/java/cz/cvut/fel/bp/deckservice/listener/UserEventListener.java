package cz.cvut.fel.bp.deckservice.listener;

import cz.cvut.fel.bp.deckservice.service.DeckService;
import cz.cvut.fel.bp.deckservice.service.event.UserDeletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventListener {

    private final DeckService deckService;

    @RabbitListener(queues = "${app.rabbitmq.queue.user-deleted}")
    public void handleUserDeletedEvent(UserDeletedEvent event) {
        log.info("Received event to delete all decks for userId: {}", event.userId());
        try {
            deckService.deleteAllDecksByUserId(event.userId());
        } catch (Exception e) {
            log.error("Failed to process UserDeletedEvent for userId: {}", event.userId(), e);
            throw e;
        }
    }
}