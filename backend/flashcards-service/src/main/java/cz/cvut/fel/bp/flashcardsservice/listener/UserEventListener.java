package cz.cvut.fel.bp.flashcardsservice.listener;

import cz.cvut.fel.bp.flashcardsservice.service.FlashcardProgressService;
import cz.cvut.fel.bp.flashcardsservice.service.event.DeckDeletedEvent;
import cz.cvut.fel.bp.flashcardsservice.service.event.UserDeletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventListener {

    private final FlashcardProgressService flashcardProgressService;

    /**
     * Naslouchá na frontě určené pro mazání balíčků.
     * Fronta se dynamicky načítá z application.yml.
     */
    @RabbitListener(queues = "${app.rabbitmq.queue.user-deleted}")
    public void handleDeckDeletedEvent(UserDeletedEvent event) {
        log.info("Received event to delete progress for user: {}", event.userId());

        try {
            flashcardProgressService.deleteAllProgressForUser(event.userId());

            log.debug("Successfully deleted flashcard progress for deckId: {}", event.userId());
        } catch (Exception e) {
            log.error("Failed to process DeckDeletedEvent for deckId: {}. Reason: {}", event.userId(), e.getMessage(), e);
            throw e;
        }
    }
}