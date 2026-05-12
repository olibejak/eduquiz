package cz.cvut.fel.bp.flashcardsservice.exception;

/**
 * Exception thrown when errors occur during flashcard review submission or processing.
 */
public class FlashcardReviewException extends FlashcardException {

    public FlashcardReviewException(String message) {
        super(message);
    }

    public FlashcardReviewException(String message, Throwable cause) {
        super(message, cause);
    }
}

