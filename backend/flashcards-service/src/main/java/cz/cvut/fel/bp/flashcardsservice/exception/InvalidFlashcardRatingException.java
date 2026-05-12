package cz.cvut.fel.bp.flashcardsservice.exception;

import cz.cvut.fel.bp.flashcardsservice.model.FlashcardRating;

/**
 * Exception thrown when an invalid or unsupported flashcard rating is provided.
 */
public class InvalidFlashcardRatingException extends FlashcardException {

    public InvalidFlashcardRatingException(FlashcardRating rating) {
        super("Invalid or unsupported flashcard rating: " + rating);
    }

    public InvalidFlashcardRatingException(String message) {
        super(message);
    }
}

