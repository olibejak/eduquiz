package cz.cvut.fel.bp.flashcardsservice.exception;

/**
 * Exception thrown when a deck cannot be found or accessed from the deck service.
 */
public class DeckNotFoundException extends FlashcardException {

    public DeckNotFoundException(Long deckId) {
        super("Deck with ID " + deckId + " not found or is not accessible");
    }

    public DeckNotFoundException(String message) {
        super(message);
    }
}

