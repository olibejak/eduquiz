package cz.cvut.fel.bp.flashcardsservice.listener;

import cz.cvut.fel.bp.flashcardsservice.service.event.DeckDeletedEvent;
import cz.cvut.fel.bp.flashcardsservice.service.FlashcardProgressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeckEventListener {

    private final FlashcardProgressService flashcardProgressService;

    /**
     * Naslouchá na frontě určené pro mazání balíčků.
     * Fronta se dynamicky načítá z application.yml.
     */
    @RabbitListener(queues = "${app.rabbitmq.queue.deck-deleted}")
    public void handleDeckDeletedEvent(DeckDeletedEvent event) {
        log.info("Received event to delete progress for deckId: {}", event.deckId());

        try {
            flashcardProgressService.deleteAllProgressForDeck(event.deckId());

            log.debug("Successfully deleted flashcard progress for deckId: {}", event.deckId());
        } catch (Exception e) {
            log.error("Failed to process DeckDeletedEvent for deckId: {}. Reason: {}", event.deckId(), e.getMessage(), e);
            throw e;
        }
    }
}