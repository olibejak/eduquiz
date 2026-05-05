package cz.cvut.fel.bp.flashcardsservice.exception;

/**
 * Base exception for flashcard service domain errors.
 */
public class FlashcardException extends RuntimeException {

    public FlashcardException(String message) {
        super(message);
    }

    public FlashcardException(String message, Throwable cause) {
        super(message, cause);
    }
}

